# Capitular Android

Aplicativo Android de leitura social desenvolvido com Kotlin e Jetpack Compose.

## Estrutura

- `androidApp/src/main/kotlin/br/imd/ufrn`, em camadas:
  - `domain`: entidades, interfaces dos repositórios e falhas (`FalhaNoRepositorio`). Não importa Android, Compose, Ktor, DataStore nem Koin.
  - `data`: implementações dos repositórios: `remoto` (Ktor, DTOs, `chamadaDeRede`) e `local` (DataStore).
  - `presentation`: telas, componentes, tema, navegação, ViewModels e `EstadoDaTela`.
  - `di`: módulos do Koin (`dadosModule`, `feedModule`, `authModule`).
  - `auth`: autenticação da Sprint 1, a ser dividida entre as camadas.
- Endereço do backend: `BuildConfig.API_BASE_URL`, padrão `http://10.0.2.2:8000` (emulador). Em aparelho físico: `./gradlew installDebug -Pcapitular.apiUrl=http://IP_DA_MAQUINA:8000`.
- `androidApp/src/main/res`: manifestos e recursos Android.
- `androidApp/src/test`: testes unitários locais e de interface (`CapitularUiTest.kt`).
- `docs`: documentação do produto.

O projeto é Android-only. Não possui targets, source sets ou módulos de iOS e desktop.

## Navegação e layout adaptativo

- O grafo usa destinos tipados com `@Serializable` e Navigation Compose.
- Em largura compacta, Feed e Ranking usam barra de navegação inferior.
- Em larguras média e expandida, a navegação muda para um trilho lateral.
- A tela de check-in pode ser aberta externamente por `capitular://check-in`.

Com o emulador em execução, o deep link pode ser demonstrado com:

```shell
../.android-runtime/sdk/platform-tools/adb -P 5038 -s emulator-5556 \
  shell am start -W -a android.intent.action.VIEW \
  -d "capitular://check-in" br.imd.ufrn
```

O botão “Voltar” e o gesto/botão do sistema retornam ao destino anterior da pilha.

## Acessibilidade (T8)

O projeto segue as diretrizes de acessibilidade do Jetpack Compose para leitores de tela (TalkBack / VoiceOver):
- **Semântica de Cabeçalho**: Uso de `Modifier.semantics { heading() }` em títulos principais, cabeçalhos de seções e títulos de livros.
- **Descrições de Imagens**: Atributos `contentDescription` descritivos em todas as imagens (ex: "Logo do Capitular").
- **Área de Toque Mínima**: Garantia de área de toque de pelo menos `48x48.dp` (`Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)`) em botões de ação e navegação soltos (como botões de voltar e sair).

## Testes de Interface (T9)

A suíte de testes de interface (`CapitularUiTest.kt`) valida os fluxos principais usando a engine do Compose (`ui-test-junit4`) com Robolectric:
1. **Teste de Renderização**: Verifica se a `FeedScreen` exibe os itens corretamente quando recebe uma lista mockada.
2. **Teste de Formulário Inválido**: Verifica se o botão "Salvar Check-in" está desabilitado (`assertIsNotEnabled()`) quando o intervalo de páginas é inválido (ex: 50 a 20).
3. **Teste de Formulário Válido**: Verifica se o botão "Salvar Check-in" está habilitado (`assertIsEnabled()`) quando o intervalo é válido.
4. **Teste de Navegação/Ação**: Verifica se o clique em "Registrar leitura" dispara o callback corretamente.
5. **Teste de Ranking**: Verifica se a `RankingScreen` renderiza corretamente a ordem e pontuação dos leitores.

## Integração Contínua (CI)

O workflow do GitHub Actions (`.github/workflows/ci.yml`) está configurado para:
- Instalar dependências gráficas necessárias no ambiente Linux (`libgl1`, `libegl1`, `libfontconfig1`).
- Executar os testes automatizados (`./gradlew :androidApp:testDebugUnitTest`).
- Compilar o aplicativo (`./gradlew assembleDebug`).

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
Quando disponível, o script reutiliza o runtime isolado em `../.android-runtime`; os logs
da execução ficam em `.run`.

## Testes

```shell
./gradlew :androidApp:testDebugUnitTest
```
