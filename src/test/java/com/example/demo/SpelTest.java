package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

@SpringBootTest
public class SpelTest {

    private ExpressionParser parser = new SpelExpressionParser();

    // plain text
    @Test
    public void test1(){
        Expression exp = parser.parseExpression("'Hello world'");
        String msg = (String)exp.getValue();
        System.out.println(msg);

        exp = parser.parseExpression("6");
        Integer value = exp.getValue(Integer.class);
        System.out.println(value * 2);
    }

    // plain text call method
    @Test
    public void test2(){
        Expression exp = parser.parseExpression("'Hello World'.concat('!')");
        String msg = (String)exp.getValue();
        System.out.println("size : " + msg.length());

        exp = parser.parseExpression("'Hello World'.length()");
        final Integer value = exp.getValue(Integer.class);
        System.out.println("size : " + value);

        exp = parser.parseExpression("'Hello World'.split(' ')[0]");
        String str = (String)exp.getValue();
        System.out.println("str = " + str);
    }

    static class User{
        public String name;
        public String email;
        public boolean admin;
        public int age;

        public User(String name, String email, boolean admin, int age) {
            this.name = name;
            this.email = email;
            this.admin = admin;
            this.age = age;
        }

        public boolean isAdmin(){
            return admin;
        }

    }

    // access object's field and method
    @Test
    public void test3(){
        User u = new User("tom", "tom@163.com", false, 30);
        Expression exp = parser.parseExpression("name");
        // inject field
        String value = (String)exp.getValue(u);
        System.out.println(value);

        exp = parser.parseExpression("admin");
        // inject field
        Boolean value2 = (Boolean)exp.getValue(u);
        System.out.println(value2);


    }

    // call obj's method , use context
    @Test
    public void test4(){
        User u = new User("tom", "tom@163.com", false, 30);
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("user", u);

        Expression exp = parser.parseExpression("#user.isAdmin()");
        Boolean boo = exp.getValue(context, Boolean.class);
        System.out.println(boo);
    }

    public static class StringHelper {
        public static boolean isValid(String url) {
            return true;
        }
    }

    @Test
    public void customFuncTest() {
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.registerFunction("isURLValid",
                    StringHelper.class.getDeclaredMethod("isValid", new Class[]{String.class}));

            context.registerFunction("isUrl", StringHelper.class.getDeclaredMethod("isValid", String.class));
            String expression = "#isURLValid('http://google.com')";

            Boolean isValid = parser.parseExpression(expression).getValue(context, Boolean.class);
            System.out.println(isValid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // spel template
    @Test
    public void test5(){
        User u = new User("tom", "tom@163.com", false, 30);
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("user", u);
        TemplateParserContext parserContext = new TemplateParserContext("#{", "}");
        System.out.println("parserContext.isTemplate() = " + parserContext.isTemplate());

        Expression exp = parser.parseExpression("#{#user.name} is admin : #{#user.isAdmin()}",parserContext);

        final String str = (String)exp.getValue(context);
        System.out.println(str);
    }

}
