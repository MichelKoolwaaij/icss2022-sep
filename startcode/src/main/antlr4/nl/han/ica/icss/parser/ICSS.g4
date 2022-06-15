grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;


//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_PAREN: '(';
CLOSE_PAREN: ')';
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';
COMMA: ',';




//--- PARSER: ---
selector : LOWER_IDENT #TagSelector
	| CLASS_IDENT #ClassSelector
	| ID_IDENT #IdSelector;
stylerule: selector+ OPEN_BRACE styleruleBody+ CLOSE_BRACE;
elseClause: ELSE OPEN_BRACE styleruleBody+ CLOSE_BRACE;
ifClause: IF BOX_BRACKET_OPEN expression BOX_BRACKET_CLOSE OPEN_BRACE (styleruleBody|elseClause)+ CLOSE_BRACE;
declaration: LOWER_IDENT COLON expression SEMICOLON;
styleruleBody: variableAssignment|declaration|ifClause|stylerule;
expression : expression MUL expression #MultiplyOperation
	| expression PLUS expression #AddOperation
	| expression MIN expression #SubtractOperation
	| COLOR #ColorLiteral
	| PIXELSIZE #PixelLiteral
	| PERCENTAGE #PercentageLiteral
	| SCALAR #ScalarLiteral
	| (TRUE|FALSE) #BoolLiteral
	| variableReference #ExpressionVariableReference;
variableReference: CAPITAL_IDENT;
variableAssignment: variableReference ASSIGNMENT_OPERATOR expression SEMICOLON;
stylesheet: (variableAssignment|stylerule)* EOF;
