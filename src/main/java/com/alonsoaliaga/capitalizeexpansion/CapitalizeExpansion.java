package com.alonsoaliaga.capitalizeexpansion;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.Cacheable;
import me.clip.placeholderapi.expansion.Configurable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CapitalizeExpansion extends PlaceholderExpansion implements Configurable, Cacheable {
    private HashMap<String,String> stringsToReplace = new HashMap<>();
    private HashMap<Pattern,String> regexToReplace = new HashMap<>();
    public CapitalizeExpansion() {
        boolean debug = false;
        try{
            debug = getPlaceholderAPI().getPlaceholderAPIConfig().isDebugMode();
        }catch (Throwable ignored) {}
        ConfigurationSection section = getConfigSection("replace");
        if(section != null) {
            for (String string : section.getKeys(false)) {
                String replacement = section.getString(string);
                if(string.startsWith("[REGEX]")) {
                    String rawRegex = string.substring(7);
                    try{
                        if(!rawRegex.isEmpty()) {
                            regexToReplace.put(Pattern.compile(rawRegex),replacement);
                            if(debug) Bukkit.getConsoleSender().sendMessage("[Capitalize-Expansion] Added regex replacement for '"+rawRegex+"' => '"+replacement+"'");
                        }else{
                            if(debug) Bukkit.getConsoleSender().sendMessage("[Capitalize-Expansion] Regex pattern is empty. Skipping..");
                        }
                    }catch (Throwable e) {
                        if(debug) Bukkit.getConsoleSender().sendMessage("[Capitalize-Expansion] Regex pattern is invalid: '"+rawRegex+"'. Skipping..");
                    }
                }else{
                    stringsToReplace.put(string,replacement);
                    if(debug) Bukkit.getConsoleSender().sendMessage("[Capitalize-Expansion] Added replacement for '"+string+"' => '"+replacement+"'");
                }
            }
            if(debug) {
                if(stringsToReplace.isEmpty() && regexToReplace.isEmpty())
                    Bukkit.getConsoleSender().sendMessage("[Capitalize-Expansion] Couldn't load any string or regex to replace. Skipping replacement..");
                else Bukkit.getConsoleSender().sendMessage("[Capitalize-Expansion] Successfully loaded "+stringsToReplace.size()+" strings to replace and "+regexToReplace.size()+".");
            }
        }else{
            Bukkit.getConsoleSender().sendMessage("[Capitalize-Expansion] The section 'replace' is not valid. Maybe it's first install?");
            Bukkit.getConsoleSender().sendMessage("[Capitalize-Expansion] Try using '/papi reload'. For now, skipping replaces..");
        }
    }
    @Override
    public void clear() {
        stringsToReplace.clear();
        regexToReplace.clear();
    }
    @Override
    public String onPlaceholderRequest(Player p, String params){
        if(params.equalsIgnoreCase("version")) {
            return getVersion();
        }
        if(params.equalsIgnoreCase("author")) {
            return getAuthor();
        }
        if(params.startsWith("pascal-case-forced_")) { // %capitalize_pascal-case-forced_{THE_PLACEHOLDER}%
            String message = replace(PlaceholderAPI.setBracketPlaceholders(p,params.substring(19)));
            String[] words = message.split(" ");
            return Arrays.stream(words).map(this::capitalFirstLowerRest).collect(Collectors.joining(" "));
        }
        if(params.startsWith("pascal-case_")) { // %capitalize_pascal-case_{THE_PLACEHOLDER}%
            String message = replace(PlaceholderAPI.setBracketPlaceholders(p,params.substring(12)));
            String[] words = message.split(" ");
            return Arrays.stream(words).map(this::capitalFirstIgnoreRest).collect(Collectors.joining(" "));
        }
        if(params.startsWith("sentence-case-forced_")) { // %capitalize_sentence-case-forced_{THE_PLACEHOLDER}%
            return capitalFirstLowerRest(replace(PlaceholderAPI.setBracketPlaceholders(p,params.substring(21))));
        }
        if(params.startsWith("sentence-case_")) { // %capitalize_sentence-case_{THE_PLACEHOLDER}%
            return capitalFirstIgnoreRest(replace(PlaceholderAPI.setBracketPlaceholders(p,params.substring(14))));
        }
        if(params.startsWith("lowercase_")) { // %capitalize_lowercase_{THE_PLACEHOLDER}%
            return replace(PlaceholderAPI.setBracketPlaceholders(p,params.substring(10))).toLowerCase(Locale.ROOT);
        }
        if(params.startsWith("uppercase_")) { // %capitalize_uppercase_{THE_PLACEHOLDER}%
            return replace(PlaceholderAPI.setBracketPlaceholders(p,params.substring(10))).toUpperCase(Locale.ROOT);
        }
        return null;
    }
    private String replace(String string) {
        for (Map.Entry<String, String> r : stringsToReplace.entrySet()) {
            string = string.replace(r.getKey(),r.getValue());
        }
        for (Map.Entry<Pattern, String> r : regexToReplace.entrySet()) {
            string = r.getKey().matcher(string).replaceAll(r.getValue());
        }
        return string;
    }
    private String capitalFirstLowerRest(String string) {
        return string.isEmpty() ? string : string.substring(0,1).toUpperCase(Locale.ROOT) + string.substring(1).toLowerCase(Locale.ROOT);
    }
    private String capitalFirstIgnoreRest(String string) {
        return string.isEmpty() ? string : string.substring(0,1).toUpperCase(Locale.ROOT) + string.substring(1);
    }
    @Override
    public Map<String, Object> getDefaults() {
        final Map<String, Object> defaults = new LinkedHashMap<>();
        if(!configurationContains("replace")) {
            defaults.put("replace._", " ");
            defaults.put("replace.-", " ");
        }
        return defaults;
    }
    @Override
    public @NotNull String getIdentifier() {
        return "capitalize";
    }
    @Override
    public @NotNull String getAuthor() {
        return "AlonsoAliaga";
    }
    @Override
    public @NotNull String getVersion() {
        return "0.1-BETA";
    }
}