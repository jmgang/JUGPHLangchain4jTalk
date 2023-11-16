package org.jugph;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;

import java.time.LocalDate;

import static java.time.Duration.ofSeconds;

public class JUGPHMemberExtractorAIServiceExample {
    public record JUGPHMember(String firstName, String lastName, String email, JUGPHMember.Gender gender, LocalDate registrationDate) {
        enum Gender {
            MALE, FEMALE, NON_BINARY, NOT_SAID
        }

        @Override
        public String toString() {
            return "JUGPHMember {" +
                    " firstName = \"" + firstName + "\"" +
                    ", lastName = \"" + lastName + "\"" +
                    ", email = \"" + email + "\"" +
                    ", gender = \"" + gender.name().toLowerCase() + "\"" +
                    ", registrationDate = " + registrationDate +
                    " }";
        }
    }

    interface MemberExtractor {
        @UserMessage("Extract member information from the following text: {{it}}. Infer the gender if not explicitly said.")
        JUGPHMember extractMemberFrom(String text);
    }

    public static void main(String[] args) {

        var model = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .timeout(ofSeconds(120))
//                .logRequests(true)
//                .logResponses(true)
                .build();

        MemberExtractor extractor = AiServices.create(MemberExtractor.class, model);

        var text = "New member alert: Maria Clara, a passionate Java developer, has just joined the JUGPH community. " +
                "Her email, maria.clara@jugph.org, was sent out on the 17th of November, 2023.";

        JUGPHMember member = extractor.extractMemberFrom(text);

        System.out.println(member);
    }
}
