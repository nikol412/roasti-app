package org.nikol.roasti.features.users

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp

object UserTable : Table("users") {
    val id = varchar("id", 128)
    val email = varchar("email", 255).uniqueIndex()
    val username = varchar("username", 255).uniqueIndex()
    val name = varchar("name", 255).nullable()
    val avatarId = varchar("avatar_id", 255).nullable()
    val bio = text("bio").nullable()
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

fun ResultRow.toUser() = User(
    id = UserId(this[UserTable.id]),
    email = this[UserTable.email],
    username = this[UserTable.username],
    name = this[UserTable.name],
    avatarId = this[UserTable.avatarId],
    bio = this[UserTable.bio],
    createdAt = this[UserTable.createdAt],
)
