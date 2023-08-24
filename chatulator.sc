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

__config()-> {
    'commands' -> {
        'enableScientificNotation <enable>' -> 'enableSciNot',
        'configScientificNotation <bound> <magnitude>' -> 'configSciNot',
        'enableRounding <enable>' -> 'enableRound',
        'configRounding <digits>' -> 'configRound',
        'restoreDefaults' -> 'setDefault',
        'listSettings' -> 'printSettings'
    },
    'arguments' -> {
        'enable' -> {'type' -> 'bool'},
        'bound' -> {'type' -> 'string', 'suggest' -> ['upper', 'lower']},
        'magnitude' -> {'type' -> 'int', 'min' -> -13, 'max' -> 307, 'suggest' -> [6, -4]},
        'digits' -> {'type' -> 'int', 'min' -> 0, 'max' -> 13, 'suggest' -> [4]}
    },
    'command_permission' -> 'players'
};

saveSettings() -> (
    nbtTag = encode_nbt({
        'scientificNotation' -> global_sciNot,
        'rounding' -> global_round,
        'upperLimitMag' -> global_upperSciNotMag,
        'lowerLimitMag' -> global_lowerSciNotMag,
        'roundPrecision' -> global_roundPrecision
    });
    success = store_app_data(nbtTag);
    if (success,
        print(format('e Settings Saved Successfully')),
    // Else
        print(format('r Failed to Save Settings!'))
    )
);

enableSciNot(boolean) -> (
    global_sciNot = boolean;
    if (boolean,
        print('Enabled Scientific Notation'),
    // Else
        print('Disabled Scientific Notation')
    );
    saveSettings()
);

configSciNot(select, num) -> (
    if (select == 'upper',
        global_upperSciNotMag = num;
        global_upperSciNotLim = 10 ^ global_upperSciNotMag;
        print('Set upper limit to 1e' + num),
    select == 'lower', // Else if
        global_lowerSciNotMag = num;
        global_lowerSciNotLim = 10 ^ global_lowerSciNotMag;
        print('Set lower limit to 1e' + num),
    // Else
        print(format('r Invalid argument: must be \'upper\' or \'lower\'.'))    
    );
    saveSettings()
);

enableRound(boolean) -> (
    global_round = boolean;
    if (boolean,
        print('Enabled Rounding'),
    // Else
        print('Disabled Rounding')
    );
    saveSettings()
);

configRound(digits) -> (
    global_roundPrecision = digits;
    print('Set rounding precision to ' + digits + ' digits');
    saveSettings()
);

setDefault() -> (
    global_sciNot = true;
    global_round = true;
    global_lowerSciNotMag = -4;
    global_upperSciNotMag = 6;
    global_lowerSciNotLim = 10 ^ global_lowerSciNotMag;
    global_upperSciNotLim = 10 ^ global_upperSciNotMag;
    global_roundPrecision = 4;
    printSettings();
    saveSettings()
);

printSettings() -> (
    print('Rounding Enabled: ' + global_round);
    print('Rounding Precision (digits): ' + global_roundPrecision);
    print('Scientific Notation Enabled: ' + global_sciNot);
    print('Scientific Notation used for magnitudes greater than 1e' + global_upperSciNotMag + ' or less than 1e' + global_lowerSciNotMag)
);

loadSettings() -> (
    nbtTag = load_app_data();
    if (nbtTag == null,
        print(format('y No saved settings found. Configuring and storing defaults.'));
        setDefault(),
    // Else
        mapData = parse_nbt(nbtTag);
        global_sciNot = mapData:'scientificNotation';
        global_round = mapData:'rounding';
        global_lowerSciNotMag = mapData:'lowerLimitMag';
        global_upperSciNotMag = mapData:'upperLimitMag';
        global_lowerSciNotLim = 10 ^ global_lowerSciNotMag;
        global_upperSciNotLim = 10 ^ global_upperSciNotMag;
        global_roundPrecision = mapData:'roundPrecision';
        print(format('e Saved Settings Restored'))
    )
);

__on_start() -> (
    loadSettings()
);

tokenize(expression) -> (
    //print(expression);
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
    //print(tokenList);
    return (tokenList);
);

parse(tokenList) -> (
    parsedList = [];
    for (tokenList,
        if (_ ~ '^\\(',
            parsedList += parse(tokenize(_ ~ '(?<=.).*')),
        (_ ~ '^[a-z]'), // Else if
            parsedList += _ ~ '^[a-z]+';
            args = _ ~ '(?<=\\().*';
            if (args != null,
                parsedList += parse(tokenize(args))
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

expMap = {};
put(expMap, '^', _(x, y) -> (x ^ y));

multMap = {};
put(multMap, '*', _(x, y) -> (x * y));
put(multMap, '/', _(x, y) -> (x / y));

plusMap = {};
put(plusMap, '+', _(x, y) -> (x + y));
put(plusMap, '-', _(x, y) -> (x - y));

global_opMaps = [];
put(global_opMaps, null, expMap);
put(global_opMaps, null, multMap);
put(global_opMaps, null, plusMap);

binaryInfixOp(operatorMap, operator, index, workingList) -> (
    if (type(get(workingList, index - 1)) == 'list',
                arg1 = evaluate(get(workingList, index - 1)),
            // Else
                arg1 = number(get(workingList, index - 1))
            );
            if (type(get(workingList, index + 1)) == 'list',
                arg2 = evaluate(get(workingList, index + 1)),
            // Else
                arg2 = number(get(workingList, index + 1))
            );
            return (call (get(operatorMap, operator), arg1, arg2));
);

binaryInfixOpAssembleList(index, workingList, result) -> (
    newList = [];
    if (index - 2 > 0,
        for (slice(workingList, 0, index - 1),
            put(newList, null, _)
        );
    );
    put(newList, null, result);
    if (index + 2 < length(workingList),
        for (slice(workingList, index + 2),
            put(newList, null, _)
        )
    );
    return (newList);
);

evaluate(parsedList) -> (
    //print('Input:' + parsedList);
    if ((length(parsedList) == 1),
        //print('length1');
        if (has(global_constants, get(parsedList, 0)),
            return (call (get(global_constants, get(parsedList, 0)))),
        // Else
            return (number(get(parsedList, 0)))
        )
    );
    for (parsedList,
        if (has(global_constants, _),
            //print('evaluatingconstant');
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
            //print('evaluatingfunction');
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
    c_for(j = 0, j < length(global_opMaps), j = j + 1,
        for (parsedList,
            if (has(global_opMaps, j, _),
                //print('evaluating' + _);
                result = binaryInfixOp(get(global_opMaps, j), _, _i, parsedList);
                if (length(parsedList) == 3,
                    return (result),
                // Else    
                    return (evaluate(binaryInfixOpAssembleList(_i, parsedList, result)))
                )
            )
        )
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
            expression = lower(expression);
            expression = replace(expression, '(?<![\\d\\)])-(?=\\()', '-1*');
            expression = replace(expression, '(\\d)(?=[a-z])', '$1\\*');
            expression = replace(expression, '\\)(?=[\\d\\(])', '\\)\\*');
            expression = replace(expression, '(?<=\\d)\\(', '*(');
            global_answer = evaluate(parse(tokenize(expression)));
            if(global_round, global_output = round_precision(global_answer, global_roundPrecision), global_output = global_answer);
            if (global_sciNot && global_answer != 0 && ((abs(global_answer) <= global_lowerSciNotLim) || (abs(global_answer) >= global_upperSciNotLim)),
                sign = global_answer / abs(global_answer);
                exponent = floor(log10(abs(global_answer)));
                mantissa = abs(global_output) / (10 ^ exponent);
                global_output = (mantissa * sign) + 'e' + exponent
            );
            schedule(1, _() -> print('= ' + global_output))
        )
    )
