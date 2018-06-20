package com.cheese.jackson;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PolymorphicTypeHandlingAnnotations {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Zoo {
        public Animal animal;

        public Zoo(Animal animal) {
            this.animal = animal;
        }

        @JsonTypeInfo(
                use = JsonTypeInfo.Id.NAME,
                include = JsonTypeInfo.As.PROPERTY,
                property = "type")
        @JsonSubTypes({
                @JsonSubTypes.Type(value = Dog.class, name = "dog"),
                @JsonSubTypes.Type(value = Cat.class, name = "cat")
        })
        @Getter
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Animal {
            private String name;

            public Animal(String name) {
                this.name = name;
            }
        }

        @JsonTypeName("dog")
        @Getter
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Dog extends Animal {
            public double barkVolume;

            public Dog(String name) {
                super(name);
            }
        }

        @JsonTypeName("cat")
        @Getter
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Cat extends Animal {
            boolean likesCream;
            public int lives;

            public Cat(String name) {
                super(name);
            }
        }

    }


}
