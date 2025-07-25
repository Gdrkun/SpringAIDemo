package com.drk.SpringAIDemo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;


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



	@Test
	void computeVideoBitrate() throws FFmpegFrameGrabber.Exception {
		//File file = new File();
		FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("C:\\Users\\glk\\Desktop\\256x112\\5b4f03f23f7bd.mp4");
		grabber.start();

		int width = grabber.getImageWidth();
		int height = grabber.getImageHeight();
		int bitrate = grabber.getVideoBitrate(); // 单位：bps
		long duration = grabber.getLengthInTime(); // 微秒

		System.out.println("视频分辨率: " + width + "x" + height);
		System.out.println("视频码率: " + bitrate/1000 + " kbps");
		System.out.println("视频时长: " + (duration / 1_000_000.0) + " 秒");

		grabber.stop();
	}


	@Test
	void convertVideo() throws FFmpegFrameRecorder.Exception, FrameGrabber.Exception {
		long startTime = System.currentTimeMillis();
		String inputPath = "C:\\Users\\glk\\Desktop\\4K\\test4K.mp4";
		String outputPath = "C:\\Users\\glk\\Desktop\\4K\\test4KConvert2.mp4";

		int targetWidth = 1920;   // 目标宽度
		int targetHeight = 1080;  // 目标高度
		int targetBitrate = 8000_000; // 目标码率，单位bps（500kbps）

		FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputPath);
		grabber.start();

		FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputPath, targetWidth, targetHeight, grabber.getAudioChannels());
		recorder.setVideoCodec(grabber.getVideoCodec()); // 保持原视频编码
		recorder.setFormat("mp4");
		recorder.setFrameRate(grabber.getFrameRate());
		//recorder.setVideoBitrate(targetBitrate);         // 设置目标码率
		//recorder.setAudioBitrate(grabber.getAudioBitrate());
		recorder.setSampleRate(grabber.getSampleRate());
		recorder.setAudioChannels(grabber.getAudioChannels());
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // 显式设置 H264
		recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);  // 默认 AAC

		recorder.setVideoOption("preset", "ultrafast");
		recorder.setVideoOption("crf", "28"); // 压缩程度（建议 28~32）
		recorder.setVideoOption("threads", "4"); // 多线程加速
		recorder.start();

		Frame frame;
		while ((frame = grabber.grabFrame()) != null) {
			// 这里可以做帧尺寸缩放（简单示例直接写，不改变尺寸时ffmpeg内部会自动处理）
			recorder.record(frame);
		}

		recorder.stop();
		grabber.stop();

		System.out.println("转码耗时: " + (System.currentTimeMillis() - startTime) + " ms");
		System.out.println("转码完成，输出文件: " + outputPath);
	}

}
