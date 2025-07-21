package com.drk.SpringAIDemo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.Set;

@SpringBootTest
class SpringAiDemoApplicationTests {

	@Test
	void contextLoads() {
	}

	private static final Set<String> TARGET_FIELDS = Set.of("startX", "startY", "endX", "endY", "x", "y", "w", "h");

	public static void convertFieldsToInt(String inputFilePath, String outputFilePath) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(new File(inputFilePath));

		processNode(root);

		mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputFilePath), root);
	}

	private static void processNode(JsonNode node) {
		if (node instanceof ObjectNode objectNode) {
			objectNode.fieldNames().forEachRemaining(field -> {
				JsonNode child = objectNode.get(field);
				if (TARGET_FIELDS.contains(field) && child.isNumber()) {
					int intValue = (int) Math.round(child.doubleValue()); // 可改为 round
					objectNode.put(field, intValue);
				} else {
					processNode(child);
				}
			});
		} else if (node instanceof ArrayNode arrayNode) {
			for (JsonNode item : arrayNode) {
				processNode(item);
			}
		}
	}

	@Test
	void test() throws IOException {
		try {
			convertFieldsToInt("C:\\Users\\glk\\Desktop\\pr.json", "C:\\Users\\glk\\Desktop\\pout.json");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
