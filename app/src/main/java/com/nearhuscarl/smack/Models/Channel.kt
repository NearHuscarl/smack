package com.nearhuscarl.smack.Models

class Channel(val name: String, val description: String, val id: String) {
    override fun toString(): String {
        return "#$name"
    }
}