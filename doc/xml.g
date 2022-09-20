token{
  LT: "<" -> in_tag;
  #S: [ \r\n\t]+;
  #ident: [a-zA-Z] [a-zA-Z0-9_]*;
  in_tag{
    TAG_NAME: ident -> attr_mode;
  }
  attr_mode{
    GT: ">" -> DEFAULT;
    EMPTY_END: "/>" -> DEFAULT;
    WS: S -> skip;
    ATTR_NAME: ident -> attr_eq;
  }
  attr_eq{
    EQ: "=" -> attr_value;
  }
  attr_value{
   ATTR_VALUE: [:string:] -> next_attr;
  }
  next_attr{
   GT: ">" -> DEFAULT;
   EMPTY_END: "/>" -> DEFAULT;
   WS: S ->  attr_mode, skip;
  }
}

element: "<" TAG_NAME attr* end;
attr: ATTR_NAME EQ ATTR_VALUE;
end: GT | EMPTY_END;