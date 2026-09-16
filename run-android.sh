#!/usr/bin/env bash

set -Eeuo pipefail

readonly SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
readonly PROJECT_ROOT="$SCRIPT_DIR"
readonly BACKEND_ROOT="$(cd -- "$PROJECT_ROOT/../capitular-backend" 2>/dev/null && pwd || true)"

AVD_NAME="capitular_isolated_api36"
EMULATOR_PORT="5556"
PORT_EXPLICIT=false
SDK_OVERRIDE=""
START_BACKEND=true
CLEAN_BOOT=false
BOOT_TIMEOUT_SECONDS=240

usage() {
    cat <<'EOF'
Uso: ./run-android.sh [opções]

Inicia o backend com Docker, abre um emulador exclusivo do Capitular,
instala o APK de debug e abre o aplicativo.

Opções:
  --no-backend       Não inicia o backend/PostgreSQL com Docker.
  --clean            Apaga os dados do AVD exclusivo antes de iniciar.
  --avd NOME         Nome do AVD isolado (padrão: capitular_isolated_api36).
  --port PORTA       Porta par do emulador (padrão: 5556).
  --sdk CAMINHO      Caminho do Android SDK.
  --timeout SEGUNDOS Tempo máximo de boot (padrão: 240).
  -h, --help         Mostra esta ajuda.

Exemplos:
  ./run-android.sh
  ./run-android.sh --clean
  ./run-android.sh --no-backend --port 5558
EOF
}

fail() {
    printf 'Erro: %s\n' "$*" >&2
    exit 1
}

log() {
    printf '\n==> %s\n' "$*"
}

while (($# > 0)); do
    case "$1" in
        --no-backend)
            START_BACKEND=false
            shift
            ;;
        --clean)
            CLEAN_BOOT=true
            shift
            ;;
        --avd)
            (($# >= 2)) || fail "--avd exige um nome."
            AVD_NAME="$2"
            shift 2
            ;;
        --port)
            (($# >= 2)) || fail "--port exige um número."
            EMULATOR_PORT="$2"
            PORT_EXPLICIT=true
            shift 2
            ;;
        --sdk)
            (($# >= 2)) || fail "--sdk exige um caminho."
            SDK_OVERRIDE="$2"
            shift 2
            ;;
        --timeout)
            (($# >= 2)) || fail "--timeout exige um número de segundos."
            BOOT_TIMEOUT_SECONDS="$2"
            shift 2
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            fail "opção desconhecida: $1"
            ;;
    esac
done

[[ "$EMULATOR_PORT" =~ ^[0-9]+$ ]] || fail "a porta do emulador deve ser numérica."
((EMULATOR_PORT >= 5554 && EMULATOR_PORT <= 5682)) || fail "use uma porta entre 5554 e 5682."
((EMULATOR_PORT % 2 == 0)) || fail "a porta do emulador deve ser par."
[[ "$BOOT_TIMEOUT_SECONDS" =~ ^[0-9]+$ ]] || fail "o timeout deve ser numérico."

resolve_android_sdk() {
    local candidate=""
    local sdk_from_properties=""
    local adb_on_path=""
    local running_sdk=""
    local -a candidates=()

    if [[ -n "$SDK_OVERRIDE" ]]; then
        candidates+=("$SDK_OVERRIDE")
    fi
    if [[ -n "${ANDROID_SDK_ROOT:-}" ]]; then
        candidates+=("$ANDROID_SDK_ROOT")
    fi
    if [[ -n "${ANDROID_HOME:-}" ]]; then
        candidates+=("$ANDROID_HOME")
    fi
    if [[ -f "$PROJECT_ROOT/local.properties" ]]; then
        sdk_from_properties="$(sed -n 's/^sdk\.dir=//p' "$PROJECT_ROOT/local.properties" | tail -n 1)"
        if [[ -n "$sdk_from_properties" ]]; then
            candidates+=("$sdk_from_properties")
        fi
    fi

    running_sdk="$(
        ps -axo command= 2>/dev/null |
            awk '/\/emulator\/qemu\/.*qemu-system/ { sub(/\/emulator\/qemu\/.*/, "", $1); print $1; exit }'
    )"
    if [[ -n "$running_sdk" ]]; then
        candidates+=("$running_sdk")
    fi

    candidates+=(
        "/private/tmp/capitular-android-codex/sdk"
        "/Users/${USER}/Library/Android/sdk"
        "/Users/${USER}/Android/Sdk"
        "/opt/android-sdk"
    )

    adb_on_path="$(command -v adb || true)"
    if [[ -n "$adb_on_path" ]]; then
        candidates+=("$(cd -- "$(dirname -- "$adb_on_path")/.." && pwd)")
    fi

    for candidate in "${candidates[@]}"; do
        if [[ -d "$candidate" ]]; then
            printf '%s\n' "$candidate"
            return 0
        fi
    done
    return 1
}

SDK_ROOT="$(resolve_android_sdk || true)"
if [[ -z "$SDK_ROOT" ]]; then
    fail "Android SDK não encontrado. Instale-o pelo Android Studio ou execute com --sdk /caminho/do/sdk."
fi

export ANDROID_SDK_ROOT="$SDK_ROOT"
export ANDROID_HOME="$SDK_ROOT"

SDK_PARENT="$(cd -- "$SDK_ROOT/.." && pwd)"
if [[ -d "$SDK_PARENT/avd" && -d "$SDK_PARENT/user" ]]; then
    ISOLATED_RUNTIME_ROOT="$SDK_PARENT"
    export ANDROID_AVD_HOME="$ISOLATED_RUNTIME_ROOT/avd"
    export ANDROID_EMULATOR_HOME="$ISOLATED_RUNTIME_ROOT/user"
    export ANDROID_USER_HOME="$ISOLATED_RUNTIME_ROOT/user"
    if [[ -d "$ISOLATED_RUNTIME_ROOT/gradle" ]]; then
        export GRADLE_USER_HOME="$ISOLATED_RUNTIME_ROOT/gradle"
    else
        export GRADLE_USER_HOME="$PROJECT_ROOT/.gradle-user-home"
    fi
else
    ISOLATED_RUNTIME_ROOT=""
    export ANDROID_AVD_HOME="$PROJECT_ROOT/.android/avd"
    export ANDROID_EMULATOR_HOME="$PROJECT_ROOT/.android/emulator-home"
    export ANDROID_USER_HOME="$PROJECT_ROOT/.android/user-home"
    export GRADLE_USER_HOME="$PROJECT_ROOT/.gradle-user-home"
fi

readonly RUN_STATE_DIR="$PROJECT_ROOT/.run"

mkdir -p "$ANDROID_AVD_HOME" "$ANDROID_EMULATOR_HOME" "$ANDROID_USER_HOME" "$GRADLE_USER_HOME" "$RUN_STATE_DIR"

find_command_line_tool() {
    local tool_name="$1"
    find "$SDK_ROOT/cmdline-tools" -type f -path "*/bin/$tool_name" -perm -111 -print 2>/dev/null | sort | tail -n 1
}

SDK_MANAGER="$(find_command_line_tool sdkmanager)"
AVD_MANAGER="$(find_command_line_tool avdmanager)"
EMULATOR_BIN="$SDK_ROOT/emulator/emulator"
ADB_BIN="$SDK_ROOT/platform-tools/adb"

case "$(uname -m)" in
    arm64|aarch64)
        SYSTEM_IMAGE_ABI="arm64-v8a"
        ;;
    *)
        SYSTEM_IMAGE_ABI="x86_64"
        ;;
esac

readonly SYSTEM_IMAGE="system-images;android-36;google_apis;${SYSTEM_IMAGE_ABI}"
readonly SYSTEM_IMAGE_DIR="$SDK_ROOT/system-images/android-36/google_apis/$SYSTEM_IMAGE_ABI"

avd_is_configured() {
    [[ -f "$ANDROID_AVD_HOME/$AVD_NAME.ini" || -d "$ANDROID_AVD_HOME/$AVD_NAME.avd" ]]
}

install_missing_sdk_packages() {
    local -a packages=()

    [[ -x "$ADB_BIN" ]] || packages+=("platform-tools")
    [[ -x "$EMULATOR_BIN" ]] || packages+=("emulator")
    [[ -d "$SDK_ROOT/platforms/android-36" ]] || packages+=("platforms;android-36")
    [[ -d "$SDK_ROOT/build-tools/36.0.0" ]] || packages+=("build-tools;36.0.0")
    # Um AVD existente pode usar outra variante da imagem (por exemplo,
    # "default"). Só precisamos baixar a imagem escolhida quando for criar
    # um AVD novo.
    if ! avd_is_configured && [[ ! -f "$SYSTEM_IMAGE_DIR/package.xml" ]]; then
        packages+=("$SYSTEM_IMAGE")
    fi

    if ((${#packages[@]} == 0)); then
        return 0
    fi
    [[ -x "$SDK_MANAGER" ]] || fail "faltam componentes do SDK e sdkmanager não foi encontrado em $SDK_ROOT/cmdline-tools."

    log "Instalando componentes ausentes do Android SDK"
    if ! "$SDK_MANAGER" --sdk_root="$SDK_ROOT" --install "${packages[@]}"; then
        fail "não foi possível instalar o SDK. Aceite as licenças com '$SDK_MANAGER --licenses' e tente novamente."
    fi
}

install_missing_sdk_packages

[[ -x "$ADB_BIN" ]] || fail "adb não encontrado em $ADB_BIN."
[[ -x "$EMULATOR_BIN" ]] || fail "emulator não encontrado em $EMULATOR_BIN."
[[ -x "$AVD_MANAGER" ]] || fail "avdmanager não encontrado no Android SDK."
if ! avd_is_configured && [[ ! -f "$SYSTEM_IMAGE_DIR/package.xml" ]]; then
    fail "imagem do sistema não encontrada: $SYSTEM_IMAGE"
fi

detect_isolated_adb() {
    local server_port="5038"
    local serial=""
    local running_avd=""

    [[ -n "$ISOLATED_RUNTIME_ROOT" ]] || return 0

    # Mantém servidor, chaves e dispositivos ADB do Capitular separados do
    # Android Studio e de outros projetos.
    export ANDROID_ADB_SERVER_PORT="$server_port"
    if ! "$ADB_BIN" -P "$server_port" devices 2>/dev/null | grep -Eq '^emulator-[0-9]+[[:space:]]+device'; then
        return 0
    fi

    if [[ "$PORT_EXPLICIT" == true ]]; then
        return 0
    fi

    while read -r serial _; do
        [[ "$serial" == emulator-* ]] || continue
        running_avd="$($ADB_BIN -s "$serial" emu avd name 2>/dev/null | head -n 1 | tr -d '\r' || true)"
        if [[ "$running_avd" == "$AVD_NAME" ]]; then
            EMULATOR_PORT="${serial#emulator-}"
            return
        fi
    done < <("$ADB_BIN" devices | awk '$2 == "device" { print $1, $2 }')
    return 0
}

detect_isolated_adb

[[ "$EMULATOR_PORT" =~ ^[0-9]+$ ]] || fail "a porta detectada do emulador é inválida."
((EMULATOR_PORT >= 5554 && EMULATOR_PORT <= 5682)) || fail "a porta detectada deve ficar entre 5554 e 5682."
((EMULATOR_PORT % 2 == 0)) || fail "a porta detectada do emulador deve ser par."

readonly EMULATOR_LOG="$RUN_STATE_DIR/emulator-${EMULATOR_PORT}.log"
readonly EMULATOR_PID_FILE="$RUN_STATE_DIR/emulator-${EMULATOR_PORT}.pid"
readonly EMULATOR_LAUNCHD_LABEL="br.imd.ufrn.capitular.emulator.${EMULATOR_PORT}"

create_isolated_avd() {
    local device_id="pixel_6"

    if "$EMULATOR_BIN" -list-avds | grep -Fxq "$AVD_NAME"; then
        return 0
    fi

    if ! "$AVD_MANAGER" list device | grep -q '"pixel_6"'; then
        device_id="pixel"
    fi

    log "Criando AVD isolado $AVD_NAME em $ANDROID_AVD_HOME"
    printf 'no\n' | "$AVD_MANAGER" create avd \
        --name "$AVD_NAME" \
        --package "$SYSTEM_IMAGE" \
        --device "$device_id"
}

start_backend() {
    [[ "$START_BACKEND" == true ]] || return 0
    [[ -n "$BACKEND_ROOT" && -f "$BACKEND_ROOT/compose.yaml" ]] || fail "backend não encontrado em ../capitular-backend. Use --no-backend para ignorar."
    command -v docker >/dev/null 2>&1 || fail "Docker não está instalado. Use --no-backend para iniciar apenas o app."
    docker info >/dev/null 2>&1 || fail "Docker não está em execução. Abra o Docker Desktop e tente novamente."

    log "Subindo API e PostgreSQL isolados com Docker"
    (
        cd "$BACKEND_ROOT"
        docker compose up -d --build
    )

    log "Aguardando a API ficar pronta"
    local deadline=$((SECONDS + 180))
    until curl --fail --silent --show-error http://127.0.0.1:8000/health/ready >/dev/null 2>&1; do
        ((SECONDS < deadline)) || fail "a API não ficou pronta. Consulte: cd '$BACKEND_ROOT' && docker compose logs api"
        sleep 2
    done
    return 0
}

create_isolated_avd
start_backend

readonly EMULATOR_SERIAL="emulator-${EMULATOR_PORT}"

if [[ "$CLEAN_BOOT" == true ]] && "$ADB_BIN" -s "$EMULATOR_SERIAL" get-state >/dev/null 2>&1; then
    log "Encerrando o AVD exclusivo para limpar os dados"
    "$ADB_BIN" -s "$EMULATOR_SERIAL" emu kill >/dev/null
    shutdown_deadline=$((SECONDS + 30))
    while "$ADB_BIN" -s "$EMULATOR_SERIAL" get-state >/dev/null 2>&1; do
        ((SECONDS < shutdown_deadline)) || fail "o emulador não encerrou dentro de 30 segundos."
        sleep 1
    done
fi

resize_clean_avd_partition() {
    local avd_config="$ANDROID_AVD_HOME/$AVD_NAME.avd/config.ini"
    local temporary_config="$avd_config.tmp.$$"
    local data_size_bytes="2147483648"

    [[ "$CLEAN_BOOT" == true && -f "$avd_config" ]] || return 0

    awk -v size="$data_size_bytes" '
        BEGIN { updated = 0 }
        /^disk\.dataPartition\.size[[:space:]]*=/ {
            print "disk.dataPartition.size = " size
            updated = 1
            next
        }
        { print }
        END {
            if (!updated) {
                print "disk.dataPartition.size = " size
            }
        }
    ' "$avd_config" >"$temporary_config"
    mv "$temporary_config" "$avd_config"
}

resize_clean_avd_partition

reset_clean_avd_data() {
    local avd_dir="$ANDROID_AVD_HOME/$AVD_NAME.avd"
    local data_file=""
    local -a data_files=(
        userdata-qemu.img
        userdata-qemu.img.qcow2
        cache.img
        cache.img.qcow2
        encryptionkey.img
        encryptionkey.img.qcow2
        sdcard.img.qcow2
    )

    [[ "$CLEAN_BOOT" == true && -d "$avd_dir" ]] || return 0

    for data_file in "${data_files[@]}"; do
        rm -f -- "$avd_dir/$data_file"
    done
}

reset_clean_avd_data

start_emulator() {
    local emulator_pid=""
    local launchd_domain="gui/$(id -u)"
    local launchd_target="$launchd_domain/$EMULATOR_LAUNCHD_LABEL"
    local -a emulator_args=(
        -avd "$AVD_NAME"
        -port "$EMULATOR_PORT"
        -no-snapshot
        -no-boot-anim
        -no-audio
        -no-metrics
        -camera-back virtualscene
        -gpu auto
    )

    if command -v launchctl >/dev/null 2>&1 && launchctl print "$launchd_domain" >/dev/null 2>&1; then
        # launchd desacopla o emulador do terminal que executou este script.
        launchctl remove "$EMULATOR_LAUNCHD_LABEL" >/dev/null 2>&1 || true
        launchctl submit \
            -l "$EMULATOR_LAUNCHD_LABEL" \
            -o "$EMULATOR_LOG" \
            -e "$EMULATOR_LOG" \
            -- /usr/bin/env \
            "ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT" \
            "ANDROID_HOME=$ANDROID_HOME" \
            "ANDROID_AVD_HOME=$ANDROID_AVD_HOME" \
            "ANDROID_EMULATOR_HOME=$ANDROID_EMULATOR_HOME" \
            "ANDROID_USER_HOME=$ANDROID_USER_HOME" \
            "ANDROID_ADB_SERVER_PORT=${ANDROID_ADB_SERVER_PORT:-5037}" \
            "$EMULATOR_BIN" "${emulator_args[@]}"

        for _ in {1..20}; do
            emulator_pid="$(launchctl print "$launchd_target" 2>/dev/null | awk '$1 == "pid" && $2 == "=" { print $3; exit }' || true)"
            [[ -n "$emulator_pid" ]] && break
            sleep 0.25
        done
    else
        nohup "$EMULATOR_BIN" "${emulator_args[@]}" >"$EMULATOR_LOG" 2>&1 &
        emulator_pid=$!
    fi

    [[ -n "$emulator_pid" ]] || fail "não foi possível obter o PID do emulador. Consulte: $EMULATOR_LOG"
    printf '%s\n' "$emulator_pid" >"$EMULATOR_PID_FILE"
}

if ! "$ADB_BIN" -s "$EMULATOR_SERIAL" get-state >/dev/null 2>&1; then
    log "Abrindo o emulador $AVD_NAME em $EMULATOR_SERIAL"
    start_emulator
fi

log "Aguardando o Android concluir o boot"
boot_deadline=$((SECONDS + BOOT_TIMEOUT_SECONDS))
while true; do
    boot_completed="$($ADB_BIN -s "$EMULATOR_SERIAL" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)"
    if [[ "$boot_completed" == "1" ]]; then
        break
    fi
    if [[ -f "$EMULATOR_PID_FILE" ]]; then
        emulator_pid="$(cat "$EMULATOR_PID_FILE")"
        if [[ -n "$emulator_pid" ]] && ! kill -0 "$emulator_pid" 2>/dev/null; then
            tail -n 40 "$EMULATOR_LOG" >&2 || true
            fail "o emulador encerrou antes de concluir o boot."
        fi
    fi
    ((SECONDS < boot_deadline)) || fail "timeout aguardando o boot. Log: $EMULATOR_LOG"
    sleep 2
done

"$ADB_BIN" -s "$EMULATOR_SERIAL" shell input keyevent 82 >/dev/null 2>&1 || true

log "Compilando e instalando o Capitular"
"$PROJECT_ROOT/gradlew" --project-dir "$PROJECT_ROOT" :androidApp:installDebug

log "Abrindo o aplicativo"
"$ADB_BIN" -s "$EMULATOR_SERIAL" shell am force-stop br.imd.ufrn
"$ADB_BIN" -s "$EMULATOR_SERIAL" shell am start -W -n br.imd.ufrn/.MainActivity

printf '\nCapitular iniciado com sucesso.\n'
printf 'Emulador: %s (%s)\n' "$AVD_NAME" "$EMULATOR_SERIAL"
printf 'API: http://127.0.0.1:8000\n'
printf 'Log do emulador: %s\n' "$EMULATOR_LOG"
