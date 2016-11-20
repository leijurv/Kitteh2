/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler.preprocess;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author leijurv
 */
public class FindBlankCharacters {
    public static List<Character> findBlankChars(String input) {
        return input.chars().parallel().distinct().mapToObj(ch -> (char) ch).filter(ch -> {
            BufferedImage wew = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            wew.getGraphics().drawString(ch + "", 25, 25);
            for (int x = 0; x < 50; x++) {
                for (int y = 0; y < 50; y++) {
                    if (wew.getRGB(x, y) != -16777216) {
                        return false;
                    }
                }
            }
            return true;
        }).collect(Collectors.toList());
    }
}
