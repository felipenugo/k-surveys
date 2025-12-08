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

        if (typeQuestion.equals(TypeQuestion.MULTIPLE_CHOICE)) {
            // Compatibilidad: algunos JSON antiguos usaban la clave 'optionQuestions' en lugar de 'options'
            JsonObject copy = jsonObject.deepCopy();
            if (!copy.has("options") && copy.has("optionQuestions")) {
                copy.add("options", copy.get("optionQuestions"));
            }

            // Si las opciones vienen como array de strings, convertirlas a objetos con 'optionText'
            if (copy.has("options") && copy.get("options").isJsonArray()) {
                JsonArray optionsArr = copy.getAsJsonArray("options");
                boolean needTransform = false;
                for (JsonElement el : optionsArr) {
                    if (el.isJsonPrimitive()) {
                        needTransform = true;
                        break;
                    }
                }
                if (needTransform) {
                    JsonArray newOpts = new JsonArray();
                    int idx = 0;
                    String surveyId = copy.has("SURVEY_ID") ? copy.get("SURVEY_ID").getAsString() : "";
                    for (JsonElement el : optionsArr) {
                        JsonObject optObj = new JsonObject();
                        optObj.addProperty("questionIndex", idx);
                        optObj.addProperty("surveyId", surveyId);
                        if (el.isJsonPrimitive()) {
                            optObj.addProperty("optionText", el.getAsString());
                        } else if (el.isJsonObject() && el.getAsJsonObject().has("optionText")) {
                            optObj.addProperty("optionText", el.getAsJsonObject().get("optionText").getAsString());
                        } else {
                            optObj.addProperty("optionText", el.toString());
                        }
                        newOpts.add(optObj);
                        idx++;
                    }
                    copy.add("options", newOpts);
                }
            }

            return context.deserialize(copy, MultipleChoiceQuestion.class);
        } else
            return DELEGATE_GSON.fromJson(json, Question.class);
    }
}
