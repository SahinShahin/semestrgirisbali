package com.example.semestr.semestrgirisbali.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class BaliController {

    @GetMapping("/")
    public String showForm() {
        return "index";
    }

    @PostMapping("/hesabla")
    public String calculateBall(
            @RequestParam(value = "seminarBallari", required = false) String[] seminarBallari,
            @RequestParam(value = "kollokviumBallari", required = false) String[] kollokviumBallari,
            @RequestParam(value = "serbestIsBal", required = false) String serbestIsBal,
            @RequestParam(value = "kursIsi", required = false) String kursIsi,
            @RequestParam(value = "kursIsiBallari", required = false) String kursIsiBallari,
            @RequestParam(value = "davamiyyet", required = false) String davamiyyet,
            @RequestParam(value = "dersSaati", required = false) String dersSaati,
            Model model) {
    
        try {
            // Seminar ballarını siyahıya çevirib ortalamasını hesablamaq
            List<Integer> seminarBalList = parseAndFilterList(seminarBallari);
            double seminarOrta = seminarBalList.stream().mapToInt(Integer::intValue).average().orElse(0);
    
            // Kollokvium ballarını siyahıya çevirib ortalamasını hesablamaq
            List<Integer> kollokviumBalList = parseAndFilterList(kollokviumBallari);
            double kollokviumOrta = kollokviumBalList.stream().mapToInt(Integer::intValue).average().orElse(0);
    
            // Seminar və kollokvium ortalarının cəmi və 3-ə vurulması
            double seminarKollokviumOrta = (seminarOrta + kollokviumOrta) / 2;
            double seminarKollokviumMultiplier = seminarKollokviumOrta * 3; // 3-ə vurmaq
    
            // Digər ballar
            int serbestIs = parseIntSafely(serbestIsBal);
            int davamiyyetBal = parseIntSafely(davamiyyet);
            
            // Kurs işi balı daxil edilibsə, ortalama hesablanır
            int kursIsBal = (kursIsi != null && kursIsi.equals("on")) ? parseIntSafely(kursIsiBallari) : 0;
            int ortalamaSerbestKurs = (kursIsBal > 0) ? (serbestIs + kursIsBal) / 2 : serbestIs;
    
            // Yekun balın hesablanması
            double yekunBal = seminarKollokviumMultiplier + davamiyyetBal + ortalamaSerbestKurs;
    
            // Qayıb limitinin hesablanması
            int dersSaatiInt = parseIntSafely(dersSaati);
            int qaliqQayib = parseIntSafely(davamiyyet); // Sənin qayıb saatların
            int qaliqLimiti = (int) (dersSaatiInt * 0.25); // Qayıb limiti (dərs saatlarının 25%-i)
    
            // Nəticələrin modelə əlavə olunması
            model.addAttribute("yekunBal", yekunBal);
            model.addAttribute("qaliqLimiti", qaliqLimiti);
            model.addAttribute("qaliqQayib", qaliqQayib);
    
            return "netice";
        } catch (Exception e) {
            model.addAttribute("error", "Hesablama zamanı xəta baş verdi: " + e.getMessage());
            return "index";
        }
    }

    // String array-ni int siyahısına çevirib boş və yanlış dəyərləri çıxarır
    private List<Integer> parseAndFilterList(String[] values) {
        if (values == null || values.length == 0) {
            return Arrays.asList(0);
        }
        return Arrays.stream(values)
                .filter(value -> value != null && !value.isEmpty())
                .map(value -> {
                    try {
                        return Integer.valueOf(value);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .collect(Collectors.toList());
    }

    // unnecessary temporary removed by using value directly in parseInt
    private int parseIntSafely(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Yanlış rəqəm formatı: " + value);
        }
    }
}
