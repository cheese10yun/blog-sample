#
# n = 700
#
# print(n)
# print(type(n))
#
#
# x = y = z = 700
#
# print(x, y, z)
#
#
# var = 75
#
# var = 'Change Value'
#
#
#
# print(var)
# print(type(var))
#
#
# m = 800
# z = 800
# n = 655
#
# print(id(m))
# print(id(z))
# print(id(n))
#
# print(id(m) == id(z))
# print()

def function1():
    print("This is a function")


def function2(x, y):
    return x + y


def function3(x):
    y1 = x * 10
    y2 = x * 20
    y3 = x * 30

    return {'y1': y1, 'y2': y2, 'y3': y3}


r = function3(1)
print(type(r), r, r.get('y2'))

def args_test(*args):
    for i, v in enumerate(args):
        print('Result : {}'.format(i), v)

# args_test('Lee')
# args_test('Lee', 'Park')
args_test('Lee', 'Park', 'Kim')


# try:
#     n = '100'+ 10
# except ValueError:
#     print('ValueError')


class Dog:
    species = 'firstdog'

    def __init__(self, name, age):
        self.name = name
        self.age = age


print(Dog)

a = Dog('mikky', 2)
print(a.__dict__)
