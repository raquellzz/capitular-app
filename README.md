# Capitular Android

Aplicativo Android de leitura social desenvolvido com Kotlin e Jetpack Compose.

## Estrutura

- `androidApp/src/main/kotlin`: aplicação, telas, autenticação e integração com a API.
- `androidApp/src/main/res`: manifestos e recursos Android.
- `androidApp/src/test`: testes unitários locais e de interface (`CapitularUiTest.kt`).
- `docs`: documentação do produto.

O projeto é Android-only. Não possui targets, source sets ou módulos de iOS e desktop.

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
O AVD, os caches Gradle e os logs ficam isolados em `.android`, `.gradle-user-home` e `.run`.

## Testes

```shell
./gradlew :androidApp:testDebugUnitTest
```
