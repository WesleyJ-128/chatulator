# Chatulator
A scarpet app to turn the Minecraft chat into a scientific calculator.  For information on how to install a scarpet script, see the official documentation [here](https://github.com/gnembon/fabric-carpet/blob/master/docs/scarpet/language/Overview.md#code-delivery-line-indicators).  As a scarpet script, it requires the Carpet mod to run, avalible [here](https://github.com/gnembon/fabric-carpet/releases).
# Usage
Once the script is installed and loaded with the command `/script load chatulator`, any chat message starting with `>` will be evaluated by the calculator; it will ignore everything else.  Answers with an absolute value less than 0.0001 or greater than 1000000 will be represented in scientific notation, and all answers are rounded to 4 decimal places. Type `>show` to see the exact value.
<br><b> !! Warning !! The calculator currently does not have any error checking, and may return incorrect or unpredictable results if invalid syntax is used. </b>
# Functionality
`>` "Command prompt"; start a chat message with this to tell the app it's an expression to be evaluated.  So `> 5 + 3` will return `8`, and `5 + 3` will do nothing.  The answer is printed only to the person who triggered the script, but your expression is still a regular chat message and thus visible to other players.
<br>`+ - * / ^`: Works as expected. Use with or without surrounding spaces.
<br>`()`: Work as expected. Implicit multiplication works, but don't try it with a constant (`>(pi)(5)` works just fine, but `>pi(5)` will break.  However, `>5pi` also works.)
<br>`e` and `pi`: Useful constants, use like numbers.
<br>`ans`: The last value calculated.  Note that the calculator stores the exact value, not necessarily the rounded value shown.
<br>`show`: Shows the exact stored value of `ans`, unrounded and in standard notation.
<br>`sqrt(x)`
<br>`abs(x)`
<br>`round(x)`: Rounds to the nearest integer
<br>`floor(x)`
<br>`ciel(x)`
<br>`ln(x)`
<br>`log(x)`: Base 10
<br>`sin(x)`: Default for all trig functions is degrees
<br>`cos(x)`
<br>`tan(x)`
<br>`csc(x)`
<br>`sec(x)`
<br>`cot(x)`
<br>`asin(x)`
<br>`acos(x)`
<br>`atan(x)`
<br>`rad(x)`: Number of radians in `x` degrees
<br>`deg(x)`: Number of degrees in `x` radians
