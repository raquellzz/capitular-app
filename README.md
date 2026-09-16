# Capitular Android

Aplicativo Android de leitura social desenvolvido com Kotlin e Jetpack Compose.

## Estrutura

- `androidApp/src/main/kotlin`: aplicação, telas, autenticação e integração com a API.
- `androidApp/src/main/res`: manifestos e recursos Android.
- `androidApp/src/test`: testes unitários locais.
- `docs`: documentação do produto.

O projeto é Android-only. Não possui targets, source sets ou módulos de iOS e desktop.

## Executar tudo

O script abaixo sobe a API e o PostgreSQL com Docker, cria um AVD exclusivo dentro
do projeto, abre o emulador, instala o APK e inicia o aplicativo:

```shell
./run-android.sh
```

Para começar com o estado limpo do emulador:

```shell
./run-android.sh --clean
```

Use `./run-android.sh --help` para escolher outro SDK, AVD, porta ou iniciar sem o backend.
O AVD, os caches Gradle e os logs ficam isolados em `.android`, `.gradle-user-home` e `.run`.

## Testes

```shell
./gradlew :androidApp:testDebugUnitTest
```
