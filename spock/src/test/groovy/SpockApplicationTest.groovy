import com.example.spock.CalculateTest
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForNumber
import spock.lang.Specification

import java.math.RoundingMode

class SpockApplicationTest extends Specification {

    def "음수가 들어오면 예외가 발생하는지 확인"() {
        given:
        RoundingMode 소수점버림 = RoundingMode.DOWN

        when:
        def calculate = CalculateTest.calculate(10_000L, 0.1f, 소수점버림)

        then:
        calculate == 10L
    }

    def "여러 금액의 퍼센트 계산 결과값의 소수점 버림을 검증한다."() {
        given:
        RoundingMode 소수점버림 = RoundingMode.DOWN

        expect:
        CalculateTest.calculate(amount, rate, 소수점버림) == result

        where:
        amount  | rate  | result
        10_000L | 0.1f  | 10L
        2799L   | 0.2f  | 5L
        159L    | 0.15f | 0L
        2299L   | 0.15f | 3L
    }

}
