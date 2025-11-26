package data.adapter;

import com.google.gson.*;
import domain.model.*;
import domain.model.enums.TypeQuestion;

import java.lang.reflect.Type;

public class AnswerAdapter implements JsonDeserializer<Answer> {

    @Override
    public Answer deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        JsonElement jsonTypeAnswer = jsonObject.get("typeAnswer");

        TypeQuestion typeAnswer = TypeQuestion.valueOf(jsonTypeAnswer.getAsString());

        if (typeAnswer.equals(TypeQuestion.MULTIPLE_CHOICE))
            return context.deserialize(json, MultipleChoiceAnswer.class);
        else if(typeAnswer.equals(TypeQuestion.TEXTUAL))
            return context.deserialize(json, TextualAnswer.class);
        else return context.deserialize(json, NumericalAnswer.class);
    }
}