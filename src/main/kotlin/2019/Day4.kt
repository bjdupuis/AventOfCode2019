package `2019`

fun main(args: Array<String>) {
    assert(passwordMeetsCriteria("112233"))  { "should have met" }
    assert(!passwordMeetsCriteria("123444"))  { "shouldn't have met" }
    assert(passwordMeetsCriteria("111122"))  { "should have met" }

    val input = "128392-643281"
    val rangeList = input.split("-").map(String::toInt)
    val range = IntRange(rangeList[0], rangeList[1])
    var total = 0
    for (password in range) {
        if (passwordMeetsCriteria(password.toString())) {
            total++
        }
    }

    println("Total passwords that meet criteria is $total")
}

fun passwordMeetsCriteria(password: String): Boolean {
    var highest = 0
    var doubleValue: Int? = null
    var numberFound = 0
    var foundValidDouble = false

    for (char in password) {
        val current = char.minus('0')
        if (current < highest) {
            return false
        }

        if (foundValidDouble) {
            highest = current
            continue
        }

        if (doubleValue != null && !foundValidDouble) {
            if (current != highest && numberFound == 2) {
                foundValidDouble = true
                doubleValue = null
            } else if (current != highest) {
                doubleValue = null
                numberFound = 0
            } else {
                numberFound++
            }
        } else if (!foundValidDouble) {
            if (current == highest) {
                doubleValue = current
                numberFound = 2
            }
        }
        highest = current;
    }

    return foundValidDouble || (doubleValue != null && numberFound == 2)
}