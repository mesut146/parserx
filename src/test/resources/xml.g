tokens{
  LT: "<";
  GT: ">";
  EQ: "=";
  STR: "\"" ("\\" . | [^\r\n"])* "\"";
  COMMENT: "<!--" ("-" "-" [^>] | "-" [^-] | [^-])* "-->"
  name: [a-zA-z] [a-zA-z0-9_]*;
  //Whitespace
  S  ::=  ("\\u0020" | "\u0009" | "\u000D" | "\u000A")+
  declStart: "<?xml";
  declEnd: "?>";
  VersionNum:  "1.0";
}

document  ::=  prolog element Misc*;

prolog       ::=  XMLDecl? Misc* (doctypedecl Misc*)?;
XMLDecl      ::=  ""<?xml" VersionInfo EncodingDecl? SDDecl? S? "?>";
VersionInfo  ::=  S "version" Eq ("'" VersionNum "'" | '"' VersionNum '"');
Eq           ::=  S? "=" S?;
Misc         ::=  Comment | PI | S;

tag: "<" name sp atrrs? sp? ">";
attrs: name "=" STR;