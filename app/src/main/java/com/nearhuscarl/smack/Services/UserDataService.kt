package com.nearhuscarl.smack.Services

import android.graphics.Color
import com.nearhuscarl.smack.Controllers.App
import java.util.*

object UserDataService {

    var id = ""
    var avatarColor = ""
    var avatarName = ""
    var email = ""
    var name = ""

    fun returnAvatarColor(component: String) : Int {
        // [0.133, 0.756, 0.721, 1]
        // 0.133 0.756 0.721 1
        val strippedColor = component
                .replace("[", "")
                .replace("]", "")
                .replace(",", "")

        var r = 0
        var g = 0
        var b = 0

        val scanner = Scanner(strippedColor)

        if (scanner.hasNext()) {
            r = (scanner.nextDouble() * 255).toInt()
            g = (scanner.nextDouble() * 255).toInt()
            b = (scanner.nextDouble() * 255).toInt()
        }

        return Color.rgb(r, g, b)
    }

    fun logout() {
        id = ""
        avatarColor = ""
        avatarName = ""
        email = ""
        name = ""
        App.sharedPrefs.authToken = ""
        App.sharedPrefs.userEmail = ""
        App.sharedPrefs.isLoggedIn = false
    }
}