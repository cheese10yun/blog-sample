package com.example.intellijtest

import org.junit.jupiter.api.Test


class RefactoringTest {

    /**
     * | Name                | Hot Key   | Desc           |
     * |---------------------|-----------|----------------|
     * | Move                | F6        | 파일 이동          |
     * | Rename              | ⇧ + F6    | 이름 변경          |
     * | Property Extract    | ⌥ + ⌘ + F | Property 으로 분리 |
     * | Introduce Variable  | ⌥ + ⌘ + V | 변수로 분리         |
     * | Introduce Parameter | ⌥ + ⌘ + P | Parameter 분리   |
     * | Inline              | ⌥ + ⌘ + N | Inline          |
     * | Function Extract    | ⌥ + ⌘ + M | Function 추출    |
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