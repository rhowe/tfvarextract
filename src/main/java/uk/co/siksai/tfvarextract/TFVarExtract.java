package uk.co.siksai.tfvarextract;

import com.bertramlabs.plugins.hcl4j.HCLParser;
import com.bertramlabs.plugins.hcl4j.HCLParserException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

public class TFVarExtract {
    private List<TFVariable> variables = new ArrayList<>();

    public static void main(String... args) throws IOException, HCLParserException {
        Path tfDir = Path.of(args[0]);
        TFVarExtract tfVarExtract = new TFVarExtract(tfDir.toFile());
        tfVarExtract.getVariables().forEach(variable -> System.out.println(String.format("Variable %s is type %s: %s", variable.getName(), variable.getType(), variable.getDescription())));
    }

    private TFVarExtract(File directory) throws IOException, HCLParserException {
        HCLParser parser = new HCLParser();

        for (File tfFile : Objects.requireNonNull(directory.listFiles((File ignored, String name) -> name.endsWith(".tf")))) {
            Map<String, Object> terraform = parser.parse(tfFile, StandardCharsets.UTF_8);
            Object variablesObj = terraform.get("variable");
            if (variablesObj instanceof Map) {
                Map variables = (Map) variablesObj;

                for (Map.Entry<String, Object> tfitemObj : ((Map<String,Object>)variables).entrySet()) {
                    Object typeObj= tfitemObj.getValue();
                    if (typeObj instanceof Map) {
                        Map type = (Map)typeObj;
                        this.variables.add(new TFVariable(
                                tfitemObj.getKey(),
                                (String)type.getOrDefault("description",""),
                               (String)type.getOrDefault("type","string")));
                    }
                }
            }
        }
    }

    private List<TFVariable> getVariables() {
        return variables;
    }

    static class TFVariable {
        String description;
        String name;
        String type;

        TFVariable(String name, String description, String type) {
            this.description = description;
            this.name = name;
            this.type = type;
        }

        String getDescription() {
            return description;
        }

        String getName() {
            return name;
        }

        String getType() {
            return type;
        }
    }
}
