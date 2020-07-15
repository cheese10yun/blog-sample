package com.example.querydsl;

public interface Foo {

    void aaa();

}

class AA{

    void asd(){
        Foo foo = () -> System.out.println("111");
    }

}
