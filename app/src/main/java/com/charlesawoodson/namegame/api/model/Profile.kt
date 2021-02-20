package com.charlesawoodson.namegame.api.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Profile(
    val id: String = "",
    val type: String = "",
    val slug: String = "",
    val jobTitle: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val headshot: HeadShot,
    val socialLinks: List<SocialLink>
)

@JsonClass(generateAdapter = true)
data class HeadShot(
    val type: String = "",
    val mimeType: String = "",
    val id: String = "",
    val url: String = "",
    val alt: String = "",
    val height: Int = 0,
    val width: Int = 0
)

@JsonClass(generateAdapter = true)
data class SocialLink(
    val type: String = "",
    val callToAction: String = "",
    val url: String = ""
)
