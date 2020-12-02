package calc.tax;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wujian
 * @date 2020/12/02
 */
public class TaxMain {

    public static void main(String[] args) throws IOException {

        List<String> collect = Files.lines(Paths.get("/Users/corey/IdeaProjects/calc-tax/src/main/resources/tax-qlj"), StandardCharsets.UTF_8)
                .collect(Collectors.toList());

        BigDecimal accumulatedIncome = new BigDecimal(0);
        BigDecimal accumulatedDeduction1 = new BigDecimal(0);
        BigDecimal accumulatedDeduction2 = new BigDecimal(0);
        BigDecimal accumulatedDeduction3 = new BigDecimal(0);
        BigDecimal accumulatedTax = new BigDecimal(0);

        for (String line : collect) {
            if (line.startsWith("#")) {
                continue;
            }
            String[] split = line.split("\\|");
            //月份
            BigDecimal month = new BigDecimal(split[0]);
            //本期收入
            BigDecimal income = new BigDecimal(split[1]);
            //本期减除费用(理论上固定5000)
            BigDecimal deduction1 = new BigDecimal(split[2]);
            //本期专项扣除=基本养老保险+基本医疗保险+失业保险+住房公积金
            BigDecimal deduction2 = new BigDecimal(split[3]);
            //本期其他扣除=年金+个人申报专项扣除
            BigDecimal deduction3 = new BigDecimal(split[4]);


            accumulatedIncome = accumulatedIncome.add(income).setScale(2, RoundingMode.HALF_UP);
            accumulatedDeduction1 = accumulatedDeduction1.add(deduction1).setScale(2, RoundingMode.HALF_UP);
            accumulatedDeduction2 = accumulatedDeduction2.add(deduction2).setScale(2, RoundingMode.HALF_UP);
            accumulatedDeduction3 = accumulatedDeduction3.add(deduction3).setScale(2, RoundingMode.HALF_UP);


            BigDecimal tex = accumulatedIncome
                    .subtract(accumulatedDeduction1).subtract(accumulatedDeduction2).subtract(accumulatedDeduction3).setScale(2, RoundingMode.HALF_UP);
            BigDecimal ratio = getRatio(tex.longValue()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal subtrahend = getSubtrahend(tex.longValue());
            tex = tex.multiply(ratio).subtract(subtrahend);
            tex = tex.subtract(accumulatedTax).setScale(2, RoundingMode.HALF_UP);
            if (tex.compareTo(new BigDecimal(0)) <= 0) {
                tex = new BigDecimal(0);
            }

            System.out.printf("%2s月：%10s=(%s-%s-%s-%s)*%s-%s-%s\n", month.toString(), tex.toString(),
                    accumulatedIncome.toString(),
                    accumulatedDeduction1.toString(), accumulatedDeduction2.toString(), accumulatedDeduction3.toString(),
                    ratio.toString(), subtrahend.toString(), accumulatedTax.toString());

            accumulatedTax = accumulatedTax.add(tex).setScale(2, RoundingMode.HALF_UP);
        }

    }

    //预扣率
    public static BigDecimal getRatio(long subSum) {

//        System.out.println("subSum: " + subSum);

        if (subSum <= 36000) {
            return new BigDecimal(0.03);
        } else if (subSum <= 144000) {
            return new BigDecimal(0.1);
        } else if (subSum <= 300000) {
            return new BigDecimal(0.2);
        } else if (subSum <= 420000) {
            return new BigDecimal(0.25);
        } else if (subSum <= 660000) {
            return new BigDecimal(0.3);
        } else if (subSum <= 960000) {
            return new BigDecimal(0.35);
        } else {
            return new BigDecimal(0.45);
        }
    }

    //速算扣除数
    public static BigDecimal getSubtrahend(long subSum) {
        if (subSum <= 36000) {
            return new BigDecimal(0);
        } else if (subSum <= 144000) {
            return new BigDecimal(2520);
        } else if (subSum <= 300000) {
            return new BigDecimal(16920);
        } else if (subSum <= 420000) {
            return new BigDecimal(31920);
        } else if (subSum <= 660000) {
            return new BigDecimal(52920);
        } else if (subSum <= 960000) {
            return new BigDecimal(85920);
        } else {
            return new BigDecimal(181920);
        }
    }
}
