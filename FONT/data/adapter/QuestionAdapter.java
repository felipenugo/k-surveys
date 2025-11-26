package data.adapter;

import com.google.gson.*;
import domain.model.MultipleChoiceQuestion;
import domain.model.Question;
import domain.model.enums.TypeQuestion;

import java.lang.reflect.Type;

public class QuestionAdapter implements JsonDeserializer<Question> {

    private static final Gson DELEGATE_GSON = new Gson();
    @Override
    public Question deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        JsonElement jsonTypeQuestion = jsonObject.get("typeQuestion");

        TypeQuestion typeQuestion = TypeQuestion.valueOf(jsonTypeQuestion.getAsString());

        if (typeQuestion.equals(TypeQuestion.MULTIPLE_CHOICE))
            return context.deserialize(json, MultipleChoiceQuestion.class);
        else
            return DELEGATE_GSON.fromJson(json, Question.class);
    }
}
