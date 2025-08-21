void main() {
  try (BufferedReader reader
           = new BufferedReader(
                new FileReader("input.txt"))) {
    String line;
    while ((line = reader.readLine()) != null) {
      System.out.println(line);
    }
  } catch (IOException e) {
    e.printStackTrace();
  }
}