import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;

public class ExpressionEvaluator {
    public static int evaluate(String expression) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            Object result = engine.eval(expression);
            return ((Number) result).intValue();
        } catch (Exception e) {
            throw new RuntimeException("Invalid expression: " + expression, e);
        }
    }
}