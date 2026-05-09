package org.nikol.roasti.features.comments

import org.jetbrains.exposed.v1.core.dao.id.UuidTable

object CommentTable : UuidTable(name = "comments") {}