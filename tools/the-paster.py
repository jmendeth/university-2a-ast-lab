from sys import stdin
for line in stdin:
  line = line[:-1]
  letters = line.split(" ")
  words = []
  while len(letters):
    word = letters.pop(0)
    if len(word) == 1:
      while len(letters) and len(letters[0]) == 1:
        word += letters.pop(0)
    words.append(word)
  print(" ".join(words))
