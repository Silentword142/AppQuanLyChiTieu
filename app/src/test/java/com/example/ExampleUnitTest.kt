package com.example

import org.junit.Assert.*
import org.junit.Test
import com.example.util.LocalOcrParser

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testCardNoParsing() {
    val result1 = LocalOcrParser.parseOcrText("The MB Bank GD the *1234 phat sinh gd tai Lazada")
    assertEquals("*1234", result1.accountNo)
    
    val result2 = LocalOcrParser.parseOcrText("TK cua quy khach tai VPBank co giao dich the ket thuc bang 9876")
    assertEquals("*9876", result2.accountNo)

    val result3 = LocalOcrParser.parseOcrText("giao dich bang the visa **** 1122 cua quy khach phat sinh -150.000 VND")
    assertEquals("****1122", result3.accountNo)
    
    val result4 = LocalOcrParser.parseOcrText("Giao dich the MC 542100xxxxxx4321 phat sinh -50,000 VND")
    assertEquals("542100xxxxxx4321", result4.accountNo)
    
    val result5 = LocalOcrParser.parseOcrText("The tin dung VPBank cua Quy khach bi tru 100,000đ từ thẻ x3311")
    assertEquals("x3311", result5.accountNo)

    val result6 = LocalOcrParser.parseOcrText("Techcombank \n- VND 205,200\nThẻ 4 .... .... 2717\nChi tiêu: - VND 205,200 tại SHOPEEPAYGATE...")
    assertEquals("4........2717", result6.accountNo)
    assertEquals(205200.0, result6.amount, 0.0)
    assertEquals("EXPENSE", result6.type)
  }
}
