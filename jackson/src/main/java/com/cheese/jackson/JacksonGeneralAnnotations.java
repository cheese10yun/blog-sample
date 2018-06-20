package com.cheese.jackson;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JacksonGeneralAnnotations {

    public static class Event {
        public String name;

        @JsonFormat(
                shape = JsonFormat.Shape.STRING,
                pattern = "dd-MM-yyyy hh:mm:ss")
        public Date eventDate;

        public Event(String name, Date date) {
            this.name = name;
            this.eventDate = date;
        }
    }

    public static class UnwrappedUser {
        public int id;

        @JsonUnwrapped
        public Name name;

        public UnwrappedUser(int id, Name name) {
            this.id = id;
            this.name = name;
        }

        public static class Name {
            public String firstName;
            public String lastName;

            public Name(String firstName, String lastName) {
                this.firstName = firstName;
                this.lastName = lastName;
            }
        }
    }

    public static class Views {
        public static class Public {
        }

        public static class Internal extends Public {
        }
    }

    public static class Item {
        @JsonView(Views.Public.class)
        public int id;

        @JsonView(Views.Public.class)
        public String itemName;

        @JsonView(Views.Internal.class)
        public String ownerName;

        public Item(int id, String itemName, String ownerName) {
            this.id = id;
            this.itemName = itemName;
            this.ownerName = ownerName;
        }
    }

    public static class ItemWithRef {
        public int id;
        public String itemName;

        @JsonManagedReference
        public UserWithRef owner;

        public ItemWithRef(int id, String itemName, UserWithRef owner) {
            this.id = id;
            this.itemName = itemName;
            this.owner = owner;
        }
    }

    public static class UserWithRef {
        public int id;
        public String name;

        @JsonBackReference
        public List<ItemWithRef> itemWithRefs = new ArrayList<>();

        public UserWithRef(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public void addItem(ItemWithRef item) {
            itemWithRefs.add(item);
        }
    }

    @JsonFilter("myFilter")
    public static class BeanWithFilter {
        public int id;
        public String name;

        public BeanWithFilter(int id, String name) {
            this.id= id;
            this.name = name;
        }
    }


}
