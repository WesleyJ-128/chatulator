//  The Chatulator Scarpet script allows the Minecraft chat to be used as a scientific calculator
//      Copyright (C) 2023 Wesley Johnson
//  
//      This program is free software: you can redistribute it and/or modify
//      it under the terms of the GNU General Public License as published by
//      the Free Software Foundation, either version 3 of the License, or
//      (at your option) any later version.
//  
//      This program is distributed in the hope that it will be useful,
//      but WITHOUT ANY WARRANTY; without even the implied warranty of
//      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//      GNU General Public License for more details.
//  
//      You should have received a copy of the GNU General Public License
//      along with this program.  If not, see <https://www.gnu.org/licenses/>.

tokenize(expression) -> (
    expList = split(expression);
    tokenList = [];
    subExpression = '';
    parenCount = 0;
    lastChar = '';
    for (expList,
        if (((_ ~ '[\\.\\w]') || ((_ ~ '-') && !(lastChar ~ '[\\)\\w]'))) && (parenCount == 0),
            subExpression += _,
        (_ ~ '\\(') && (parenCount == 0), // Else if
            subExpression += _;
            parenCount = 1,
        (_ ~ '\\)') && (parenCount == 1), // Else if
            parenCount = 0,
        parenCount != 0, // Else if
            subExpression += _;
            if (_ ~ '\\(',
                parenCount += 1,    
            _ ~ '\\)', //Else if
                parenCount = parenCount - 1
            ),
        // Else:
            if (subExpression != '',
                tokenList += subExpression;
                subExpression = '';
                tokenList += _,
            // Else:
                tokenList += _
            )
        );
        if ((_i + 1 == length(expList)) && (subExpression != ''),
                tokenList += subExpression;
                subExpression = ''
            );
        lastChar = _
    );
    return (tokenList);
);

parse(tokenList) -> (
    parsedList = [];
    for (tokenList,
        if (_ ~ '^\\(',
            parsedList += parse(tokenize(_ ~ '(?<=.).*')),
        (_ ~ '^[A-Za-z]'), // Else if
            parsedList += _ ~ '^[A-Za-z]+';
            args = _ ~ '(?<=\\().*';
            if (args != null,
                parsedList += parse(tokenize(_ ~ '(?<=\\().*'))
            ),
        // Else
            parsedList += _
        )
    );
    return (parsedList)
);

global_answer = 0;
global_output = 0;

global_constants = {};
put(global_constants, 'pi', _() -> pi);
put(global_constants, 'e', _() -> euler);
put(global_constants, 'ans', _() -> global_answer);

global_functions = {};
put(global_functions, 'sqrt', _(x) -> sqrt(x));
put(global_functions, 'abs', _(x) -> abs(x));
put(global_functions, 'round', _(x) -> round(x));
put(global_functions, 'floor', _(x) -> floor(x));
put(global_functions, 'ciel', _(x) -> ciel(x));
put(global_functions, 'ln', _(x) -> ln(x));
put(global_functions, 'log', _(x) -> log10(x));
put(global_functions, 'sin', _(x) -> sin(x));
put(global_functions, 'cos', _(x) -> cos(x));
put(global_functions, 'tan', _(x) -> tan(x));
put(global_functions, 'csc', _(x) -> csc(x));
put(global_functions, 'sec', _(x) -> sec(x));
put(global_functions, 'cot', _(x) -> cot(x));
put(global_functions, 'asin', _(x) -> asin(x));
put(global_functions, 'acos', _(x) -> acos(x));
put(global_functions, 'atan', _(x) -> atan(x));
put(global_functions, 'rad', _(x) -> rad(x));
put(global_functions, 'deg', _(x) -> deg(x));

global_multMap = {};
put(global_multMap, '*', _(x, y) -> (x * y));
put(global_multMap, '/', _(x, y) -> (x / y));

global_plusMap = {};
put(global_plusMap, '+', _(x, y) -> (x + y));
put(global_plusMap, '-', _(x, y) -> (x - y));

evaluate(parsedList) -> (
    //print('Input:' + parsedList);
    if ((length(parsedList) == 1),
        if (has(global_constants, get(parsedList, 0)),
            return (call (get(global_constants, get(parsedList, 0)))),
        // Else
            return (number(get(parsedList, 0)))
        )
    );
    for (parsedList,
        if (has(global_constants, _),
            result = call (get(global_constants, _));
            if (length(parsedList) == 1,
                return (result),
            // Else
                newList = [];
                if (_i - 1 > 0,
                    for (slice(parsedList, 0, _i),
                        put(newList, null, _)
                    );
                );
                put(newList, null, result);
                if (_i + 1 < length(parsedList),
                    for (slice(parsedList, _i + 1),
                        put(newList, null, _)
                    )
                );
                return (evaluate(newList))
            )
        );
    );
    for (parsedList,
        if (_ ~ '[a-z]+',
            if (type(get(parsedList, _i + 1)) == 'list',
                arg = evaluate(get(parsedList, _i + 1)),
            // Else
                arg = number(get(parsedList, _i + 1))
            );
            result = call (get(global_functions, _), arg);
            if (length(parsedList) == 2,
                return (result),
            // Else
                newList = [];
                if (_i - 1 > 0,
                    for (slice(parsedList, 0, _i),
                        put(newList, null, _)
                    );
                );
                put(newList, null, result);
                if (_i + 2 < length(parsedList),
                    for (slice(parsedList, _i + 2),
                        put(newList, null, _)
                    )
                );
                return (evaluate(newList))
            )
        );
    );
    for (parsedList,
        if (_ ~ '\\^',
            if (type(get(parsedList, _i - 1)) == 'list',
                base = evaluate(get(parsedList, _i - 1)),
            // Else
                base = number(get(parsedList, _i - 1))
            );
            if (type(get(parsedList, _i + 1)) == 'list',
                exp = evaluate(get(parsedList, _i + 1)),
            // Else
                exp = number(get(parsedList, _i + 1))
            );
            result = base ^ exp;
            if (length(parsedList) == 3,
                return (result),
            // Else
                newList = [];
                if (_i - 2 > 0,
                    for (slice(parsedList, 0, _i - 1),
                        put(newList, null, _)
                    );
                );
                put(newList, null, result);
                if (_i + 2 < length(parsedList),
                    for (slice(parsedList, _i + 2),
                        put(newList, null, _)
                    )
                );
                return (evaluate(newList))
            )
        )
    );
    for (parsedList,
        if (_ ~ '[\\*/]',
            if (type(get(parsedList, _i - 1)) == 'list',
                arg1 = evaluate(get(parsedList, _i - 1)),
            // Else
                arg1 = number(get(parsedList, _i - 1))
            );
            if (type(get(parsedList, _i + 1)) == 'list',
                arg2 = evaluate(get(parsedList, _i + 1)),
            // Else
                arg2 = number(get(parsedList, _i + 1))
            );
            result = call (get(global_multMap, _), arg1, arg2);
            if (length(parsedList) == 3,
                return (result),
            // Else
                newList = [];
                if (_i - 2 > 0,
                    for (slice(parsedList, 0, _i - 1),
                        put(newList, null, _)
                    );
                );
                put(newList, null, result);
                if (_i + 2 < length(parsedList),
                    for (slice(parsedList, _i + 2),
                        put(newList, null, _)
                    )
                );
                return (evaluate(newList))
            )
        )
    );
    for (parsedList,
        if (_ ~ '^[\\+-]$',
            if (type(get(parsedList, _i - 1)) == 'list',
                arg1 = evaluate(get(parsedList, _i - 1)),
            // Else
                arg1 = number(get(parsedList, _i - 1))
            );
            if (type(get(parsedList, _i + 1)) == 'list',
                arg2 = evaluate(get(parsedList, _i + 1)),
            // Else
                arg2 = number(get(parsedList, _i + 1))
            );
            result = call (get(global_plusMap, _), arg1, arg2);
            if (length(parsedList) == 3,
                return (result),
            // Else
                newList = [];
                if (_i - 2 > 0,
                    for (slice(parsedList, 0, _i - 1),
                        put(newList, null, _)
                    );
                );
                put(newList, null, result);
                if (_i + 2 < length(parsedList),
                    for (slice(parsedList, _i + 2),
                        put(newList, null, _)
                    )
                );
                return (evaluate(newList))
            )
        )
    );
);

round_toward_zero(x) -> (
    if (x > 0,
        return (floor(x)),
    x < 0, // Else If
        return (ceil(x)),
    // Else
        return (x)
    )
);

round_precision(x, digits) -> (
    shift = 10 ^ digits;
    return (round(x * shift) / shift)
);

__on_player_message(player, message) ->
    if (message ~ '^>',
        if (message ~ '^> ?show',
            schedule(0, _() -> print('= ' + global_answer)),
        // Else
            expression = message ~ '(?<=.).*';
            expression = replace(expression, ' ', '');
            expression = replace(expression, '(?<![\\d\\)])-(?=\\()', '-1*');
            global_answer = evaluate(parse(tokenize(expression)));
            global_output = round_precision(global_answer, 4);
            if ((global_answer <= 1e-4) || (global_answer >= 1e6),
                exponent = round_toward_zero(log10(global_answer));
                mantissa = round_precision(global_answer / (10 ^ exponent), 4);
                if (mantissa < 1,
                    mantissa = mantissa * 10;
                    exponent = exponent - 1
                );
                global_output = mantissa + 'e' + exponent
            );
            schedule(0, _() -> print('= ' + global_output))
        )
    )