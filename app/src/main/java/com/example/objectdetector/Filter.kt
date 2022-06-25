package com.example.objectdetector

object Filter {

    //error
    private const val DOG = "Dog"
    private const val JEANS = "Jeans"
    private const val VEHICLE = "Vehicle"
    private const val CAR = "Car"
    private const val BUMPER = "Bumper"
    private const val MOBILE_PHONE = "Mobile phone"
    private const val TABLE_WARE = "Tableware"
    private const val CUTLERY = "Cutlery"
    private const val METAL = "Metal"
    private const val ROOM = "Room"
    private const val BIRD = "Bird"
    private const val WING = "Wing"
    private const val EYELASH = "Eyelash"

    //ignore
    private const val MUSICAL_INSTRUMENT = "Musical instrument"
    private const val COOL = "Cool"
    private const val DUDE = "Dude"
    private const val SKY = "Sky"
    private const val FUN = "Fun"
    private const val HAT = "Hat"
    private const val SUNGLASSES = "Sunglasses"
    private const val SMILE = "Smile"
    private const val AIRCRAFT = "Aircraft"
    private const val JACKET = "Jacket"
    private const val OUTERWEAR = "Outerwear"
    private const val WOOL = "Wool"
    private const val MUSCLE = "Muscle"
    private const val TOY = "Toy"

    val errorFilter = listOf(
        DOG,
        JEANS,
        MOBILE_PHONE,
        VEHICLE,
        CAR,
        BUMPER,
        TABLE_WARE,
        CUTLERY,
        METAL,
        ROOM,
        BIRD,
        WING,
        EYELASH
    )

    val ignoreFilter = listOf(
        COOL,
        DUDE,
        MUSICAL_INSTRUMENT,
        SKY,
        FUN,
        HAT,
        SUNGLASSES,
        SMILE,
        AIRCRAFT,
        JACKET,
        OUTERWEAR,
        WOOL,
        MUSCLE,
        TOY
    )
}
