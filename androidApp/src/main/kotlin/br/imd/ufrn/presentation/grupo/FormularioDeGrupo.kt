package br.imd.ufrn.presentation.grupo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import br.imd.ufrn.domain.FalhaNoRepositorio
import br.imd.ufrn.domain.grupo.Grupo
import br.imd.ufrn.domain.grupo.GrupoRepository
import br.imd.ufrn.domain.grupo.RegrasDoGrupo
import br.imd.ufrn.presentation.mensagemParaUsuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Campos de "criar grupo" e "entrar com convite". */
data class FormularioDeGrupo(
    val nome: String = "",
    val descricao: String = "",
    val codigo: String = "",
    val enviando: Boolean = false,
    val erro: String? = null,
) {
    val podeCriar: Boolean
        get() =
            !enviando &&
                RegrasDoGrupo.validarNome(nome) == null &&
                RegrasDoGrupo.validarDescricao(descricao) == null
    val podeEntrar: Boolean get() = !enviando && RegrasDoGrupo.validarCodigo(codigo) == null
}

/**
 * Estado e ações do formulário de grupo, reaproveitados pelo [GrupoViewModel]
 * (primeiro acesso) e pelo [MeusGruposViewModel] (aba Grupos).
 * [aoConcluir] recebe o grupo criado ou em que o usuário entrou.
 */
class AcoesDoFormularioDeGrupo(
    private val repositorio: GrupoRepository,
    private val escopo: CoroutineScope,
    private val aoConcluir: suspend (Grupo) -> Unit,
) {
    private val _formulario = MutableStateFlow(FormularioDeGrupo())
    val formulario: StateFlow<FormularioDeGrupo> = _formulario.asStateFlow()

    fun alterarNome(nome: String) = _formulario.update { it.copy(nome = nome, erro = null) }

    fun alterarDescricao(descricao: String) = _formulario.update { it.copy(descricao = descricao, erro = null) }

    fun alterarCodigo(codigo: String) = _formulario.update { it.copy(codigo = codigo, erro = null) }

    fun criar() {
        val atual = _formulario.value
        val erro = RegrasDoGrupo.validarNome(atual.nome) ?: RegrasDoGrupo.validarDescricao(atual.descricao)
        if (erro != null) return _formulario.update { it.copy(erro = erro) }
        enviar { repositorio.criar(nome = atual.nome, descricao = atual.descricao) }
    }

    fun entrar() {
        val codigo = _formulario.value.codigo
        RegrasDoGrupo.validarCodigo(codigo)?.let { erro -> return _formulario.update { it.copy(erro = erro) } }
        enviar { repositorio.entrar(codigo) }
    }

    private fun enviar(acao: suspend () -> Grupo) {
        if (_formulario.value.enviando) return
        escopo.launch {
            _formulario.update { it.copy(enviando = true, erro = null) }
            try {
                val grupo = acao()
                _formulario.value = FormularioDeGrupo()
                aoConcluir(grupo)
            } catch (falha: FalhaNoRepositorio) {
                // Falhou ao salvar: mantém o que foi digitado e mostra o motivo.
                _formulario.update { it.copy(enviando = false, erro = mensagemDoEnvio(falha)) }
            }
        }
    }

    private fun mensagemDoEnvio(falha: FalhaNoRepositorio): String =
        if (falha is FalhaNoRepositorio.RespostaInvalida && falha.status == 404) {
            "Convite não encontrado. Confira o código com quem te convidou."
        } else {
            falha.mensagemParaUsuario()
        }
}

/** Eventos do formulário, para não repetir cinco lambdas em cada tela. */
data class EventosDoFormularioDeGrupo(
    val onNomeChange: (String) -> Unit = {},
    val onDescricaoChange: (String) -> Unit = {},
    val onCodigoChange: (String) -> Unit = {},
    val onCriar: () -> Unit = {},
    val onEntrar: () -> Unit = {},
)

fun AcoesDoFormularioDeGrupo.eventos() =
    EventosDoFormularioDeGrupo(
        onNomeChange = ::alterarNome,
        onDescricaoChange = ::alterarDescricao,
        onCodigoChange = ::alterarCodigo,
        onCriar = ::criar,
        onEntrar = ::entrar,
    )

/** Cartões "Criar um grupo" e "Entrar com convite", com a mensagem de erro acima. */
@Composable
fun FormulariosDeGrupo(
    formulario: FormularioDeGrupo,
    eventos: EventosDoFormularioDeGrupo,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        formulario.erro?.let { erro ->
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(text = erro, modifier = Modifier.fillMaxWidth().padding(12.dp))
            }
        }

        CartaoDeGrupo(titulo = "Criar um grupo") {
            OutlinedTextField(
                value = formulario.nome,
                onValueChange = eventos.onNomeChange,
                label = { Text("Nome do grupo") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !formulario.enviando,
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                shape = RoundedCornerShape(12.dp),
            )
            OutlinedTextField(
                value = formulario.descricao,
                onValueChange = eventos.onDescricaoChange,
                label = { Text("Descrição (opcional)") },
                supportingText = {
                    Text("${formulario.descricao.trim().length}/${RegrasDoGrupo.MAXIMO_DESCRICAO}")
                },
                isError = RegrasDoGrupo.validarDescricao(formulario.descricao) != null,
                modifier = Modifier.fillMaxWidth(),
                enabled = !formulario.enviando,
                minLines = 2,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                shape = RoundedCornerShape(12.dp),
            )
            Button(
                onClick = eventos.onCriar,
                enabled = formulario.podeCriar,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
            ) { Text("Criar grupo", fontWeight = FontWeight.Bold) }
        }

        CartaoDeGrupo(titulo = "Entrar com convite") {
            OutlinedTextField(
                value = formulario.codigo,
                onValueChange = eventos.onCodigoChange,
                label = { Text("Código de convite") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !formulario.enviando,
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
                shape = RoundedCornerShape(12.dp),
            )
            OutlinedButton(
                onClick = eventos.onEntrar,
                enabled = formulario.podeEntrar,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
            ) { Text("Entrar no grupo", fontWeight = FontWeight.Bold) }
        }
    }
}

/** Cartão com título, usado nas telas de grupo. */
@Composable
fun CartaoDeGrupo(
    titulo: String,
    modifier: Modifier = Modifier,
    conteudo: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                modifier = Modifier.semantics { heading() },
            )
            conteudo()
        }
    }
}
