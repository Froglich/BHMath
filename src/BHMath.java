/**
 * Copyright (C) 2015 Kim Lindgren, the Borderstone Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.ArrayList;

/**
 * BH as in Borderstone-Hybrid. As in, this class is a hybrid of an older
 * and a newer class, and uses the best parts of both.
 */

public class BHMath {
    private final ComponentList components = new ComponentList();
    private static final String[] operators = {"^", "*", "/", "-", "+"};

    public double parseExpression(String expression) throws NumberFormatException {
        if(expression == null || expression.equals(""))
            throw new IllegalArgumentException("Expression can not be empty or null.");

        int pStarts = expression.replaceAll("[^(]", "").length();
        int pEnds = expression.replaceAll("[^)]", "").length();

        if(pStarts > pEnds | pStarts < pEnds)
            throw new IllegalArgumentException("Not closing all parentheses.");
        else if(pStarts < pEnds)
            throw new IllegalArgumentException("Unexpected closing parenthesis.");

        String modifiedExpression = expression.replaceAll("[ \t]]", "");

        while(modifiedExpression.contains("(")) {
            modifiedExpression = solveTopParenthesis(modifiedExpression);
        }

        return Double.valueOf(parseOperators(modifiedExpression));
    }

    private static double doSimpleMath(String a, String b, String c) throws NumberFormatException{
        double _a = Double.parseDouble(a);
        double _c = Double.parseDouble(c);

        double out;

        switch(b){
            case "+":
                out = _a + _c;
                break;
            case "-":
                out = _a - _c;
                break;
            case "/":
                if(_c == 0)
                    throw new IllegalArgumentException("Divide by Zero");
                out = _a / _c;
                break;
            case "*":
                out = _a * _c;
                break;
            case "^":
                out = Math.pow(_a, _c);
                break;
            default:
                throw new IllegalArgumentException("Unknown operator: \" + b + \"");
        }

        return out;
    }

    private String solveTopParenthesis(String expression) throws NumberFormatException{
        StringBuilder sb = new StringBuilder();

        int start = expression.lastIndexOf("(");
        int end = start + expression.substring(start).indexOf(")");

        if(end == -1) throw new IllegalArgumentException("Malformed expression.");

        sb.append(expression.substring(0, start));

        String mod = expression.substring(start+1, end);
        mod = parseOperators(mod);

        sb.append(mod);
        sb.append(expression.substring(end+1));

        return sb.toString();
    }

    private String parseOperators(String expression) throws NumberFormatException {
        components.clear();

        boolean lastWasOperator = true;

        char[] statement = expression.toCharArray();

        components.add("");

        for(char c : statement){
            switch(c){
                case '-':
                    if(lastWasOperator){
                        components.put(Character.toString(c), false);
                        lastWasOperator = false;
                    }
                    else{
                        components.put(Character.toString(c), true);
                        lastWasOperator = true;
                    }
                    break;
                case '+':
                    if(lastWasOperator){
                        components.put(Character.toString(c), false);
                        lastWasOperator = false;
                    }
                    else{
                        components.put(Character.toString(c), true);
                        lastWasOperator = true;
                    }
                    break;
                case '*':
                    components.put(Character.toString(c), true);
                    lastWasOperator = true;
                    break;
                case '/':
                    components.put(Character.toString(c), true);
                    lastWasOperator = true;
                    break;
                case '^':
                    components.put(Character.toString(c), true);
                    lastWasOperator = true;
                    break;
                default:
                    if(components.isEmpty()){
                        components.add("");
                    }

                    components.put(Character.toString(c), false);

                    lastWasOperator = false;
                    break;
            }

        }

        for(String s : operators){
            int x = 0;

            while(x < components.size()){
                if(components.get(x).equals(s)){
                    components.set(x, Double.toString(doSimpleMath(components.get(x-1), s, components.get(x+1))));

                    components.remove(x+1);
                    components.remove(x-1);
                    x = 0;
                }
                else{
                    x++;
                }
            }
        }

        return components.get(0);
    }
}

class ComponentList extends ArrayList<String> {
    private int currIndex = 0;

    public void put(String s, boolean operator){
        if(!operator){
            this.set(currIndex, this.get(currIndex) + s);
        }
        else{
            this.add(s);
            this.add("");

            currIndex += 2;
        }
    }

    @Override
    public void clear(){
        currIndex = 0;
        super.clear();
    }
}