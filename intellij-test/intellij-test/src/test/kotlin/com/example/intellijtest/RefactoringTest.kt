package com.example.intellijtest

import org.junit.jupiter.api.Test

private val s = "openingHours"

class RefactoringTest {


    private val s1 = "band"

    /**
     *
     *
     * | Name                | Hot Key   | Desc           |
     * |---------------------|-----------|----------------|
     * | Move                | F6        | 파일 이동          |
     * | Rename              | ⇧ + F6    | 이름 변경          |
     * | Property Extract    | ⌥ + ⌘ + F | Property 으로 분리 |
     * | Introduce Variable  | ⌥ + ⌘ + V | 변수로 분리         |
     * | Introduce Parameter | ⌥ + ⌘ + P | Parameter 분리   |
     * | Inline              | ⌥ + ⌘ + N | 변수 머지          |
     */
    @Test
    fun `Refactoring`(address: String?) {
        Shop(
            brn = "brn",
            name = "name",
            band = "band",
            category = "category",
            email = "email",
            website = "website",
            openingHours = "openingHours",
            seatingCapacity = 1,
            rating = 1,
            address = when (address) {
                null -> "Default address"
                else -> address
            },
            addressDetail = "addressDetail",
            zipCode = "zipCode",
        )
    }

}