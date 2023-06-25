import unittest
import platform


class MyTestCase(unittest.TestCase):
     def test_something(self):
        print(platform.version())
        print(platform.python_version())
        print(platform.processor())
        # self.assertEqual(True, False)  # add assertion here



if __name__ == '__main__':
    unittest.main()


# print("hello python!")