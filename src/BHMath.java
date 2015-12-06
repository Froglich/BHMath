/*
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

/*
    Be warned! I wrote this class 2 years ago in C# and ported it to Java
    quite recently. I remember it having some quirks back then
    but while testing it now after writing the port I cant seem to find
    anything wierd going on. I have compared the output of several equations
    with the output from Python (3.3.3), and it always comes up as the same,
    I take that as some validation of its accuracy.
*/

import java.util.ArrayList;

public class BHMath {
    //Do the simple parts here, like + and -
    private static Solution doSimpleMath(String a, String o, String b){ 	// a = first value, o = operator, b = second value
        Solution result = new Solution();
        
        double _a;
        double _b;
        
        Solution test;
        
        result.success = false;
        
        test = tryParseDouble(a); //make sure it is numeric
        if(test.success){
            _a = test.output;
        }
        else{
            return result;
        }
        
        test = tryParseDouble(b); //make sure it is numeric
        if(test.success){
            _b = test.output;
        }
        else{
            return result;
        }
        
        //perform different operations depending on the operator passed to the function.
        switch(o){
            case "^":
                if(_a < 0){ result.output = -(Math.pow(Math.abs(_a), _b)); }//otherwise the result is... wierd
                else{ result.output = Math.pow(_a, _b); }
                break;
            case "*":
                result.output = _a*_b;
                break;
            case "/":
                result.output = _a/_b;
                break;
            case "+":
                result.output = _a+_b;
                break;
            case "-":
                result.output = _a-_b;
                break;
            default:
                return result;
        }       
        
        result.success = true;
        
        return result;
    }
    
    //split the string into mathematical components and work it down to only very simple statements.
    public static Solution Solve(String statement){
        Solution result = new Solution();
        Solution test;
        
        ArrayList<String> components = new ArrayList<>();
        ArrayList<Integer> reruns = new ArrayList<>();
        
        String[] operators = {"^", "*", "/", "-", "+"};//mathematical operators in the order they should be processed
        
        boolean lastWasOperator = true;
                
        int open = 0;
        
        int x;
        
        result.output = 0;
        result.success = false;

        if(statement == null || statement.equals("")){
            return result;
        }
        
        char[] st = statement.toCharArray();
        
        //split the statement into chars and separate the components
        for(char c : st){
            if(c == '(' && open == 0){//start of a partial statement
                components.add("");//Add the statement as a new component
                reruns.add(components.size()-1);//it needs further processing
                open++;
            }
            else if(c == '(' && open > 0){//start of a partial statement inside another partial statement
                components.set(components.size()-1, components.get(components.size()-1) + c);//just add it to the current statement
                open++;//and make sure we dont close them too early
            }
            else if(c == ')' && open > 0){//closing a partial statement
                if(open - 1 != 0){
                    components.set(components.size()-1, components.get(components.size()-1) + c);
                }
                open--;
            }
            else if(c == ')' && open == 0){//too many ")" in the statement
                return result;
            }
            //FROM HERE, ADD OPERATORS AS NEW COMPONENTS
            else if(open == 0 && c == '+' && !lastWasOperator){//only add as new component if the last char was not an operator
                components.add(Character.toString(c));//so do not add it if it was part of a number (i.e. +1)
                components.add("");
                
                lastWasOperator = true;
            }
            else if(open == 0 && c == '-' && !lastWasOperator){//only add as new component if the last char was not an operator
                components.add(Character.toString(c));//so do not add it if it was part of a number (i.e. -1)
                components.add("");
                
                lastWasOperator = true;
            }
            else if(open == 0 && c == '*'){
                components.add(Character.toString(c));
                components.add("");
                
                lastWasOperator = true;
            }
            else if(open == 0 && c == '^'){
                components.add(Character.toString(c));
                components.add("");
                
                lastWasOperator = true;
            }
            else if(open == 0 && c == '/'){
                components.add(Character.toString(c));
                components.add("");
                
                lastWasOperator = true;
            }
            else if(c != ' '){//if its not an operator or a space, add the char to the current component.
                if(components.isEmpty()){//in case its the first one added.
                    components.add("");
                }
                
                components.set(components.size()-1, components.get(components.size()-1) + c);
                
                lastWasOperator = false;//+ and - can be added as operators
            }            
        }
        
        if(open > 0){//if not all statements were closed, throw an error!
            return result;
        }
        
        if(reruns.size() > 0){//run the partial components again.
            for(int q : reruns){
                test = Solve(components.get(q));
                if(test.success){
                    components.remove(q);
                    components.add(q, Double.toString(test.output));
                }
                else{
                    return result;
                }
            }   
        }
                
        //remove empty items from the list of components
        for(int b = 0; b < components.size(); b++){
            if(components.get(b).equals("") | components.get(b) == null){
                components.remove(b);
            }
        }
        
        //now only simple mathematical components are left and we can solve the problem.
        for(String s : operators){
            x = 0;
            
            while(x < components.size()){
                if(components.get(x).equals(s)){
                    test = doSimpleMath(components.get(x-1), s, components.get(x+1));
                    if(test.success){
                        components.set(x, Double.toString(test.output));
                    }
                    else{
                        return result;
                    }
                    
                    components.remove(x+1);
                    components.remove(x-1);
                    x = 0;
                }
                else{
                    x++;
                }
            }
        }

        if(!(tryParseDouble(components.get(0)).success)){
            return result;
        }

        result.success = true;
        result.output = Double.parseDouble(components.get(0));
        
        return result;
    }
    
    private static Solution tryParseDouble(String value){
        Solution s = new Solution();
        
        try{
            s.output = Double.parseDouble(value);
            s.success = true;
        }
        catch(NumberFormatException e){
            s.success = false;
        }
        
        return s;
    }
}
