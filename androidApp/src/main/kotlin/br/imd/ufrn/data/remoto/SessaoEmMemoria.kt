package br.imd.ufrn.data.remoto

/**
 * Guarda o token de acesso enquanto o app está aberto e o entrega ao cliente HTTP.
 *
 * Provisório: a tarefa de Auth (T05/T07) troca isto por uma sessão persistida
 * no DataStore, mantendo a interface [ProvedorDeToken].
 */
class SessaoEmMemoria : ProvedorDeToken {
    @Volatile
    var token: String? = null

    override fun tokenDeAcesso(): String? = token
}
