package pt.isel.daw.gomoku.domain.users

/**
 *  Represents an authenticated user.
 *  @property user The user.
 *  @property token The authentication token.
 * */

class AuthenticatedUser(
    val user: User,
    val token: String
)