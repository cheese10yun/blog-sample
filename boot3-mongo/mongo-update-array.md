```
db.order_item.updateOne(
  { _id: ObjectId("문서_ID") },
  {
    $set: {
      "items.$[elem1].price": 300,
      "items.$[elem2].price": 400
    }
  },
  {
    arrayFilters: [
      { "elem1.name": "item1" },
      { "elem2.name": "item2" }
    ]
  }
)
```