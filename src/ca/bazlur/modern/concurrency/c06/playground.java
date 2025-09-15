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


    try (FileChannel channel = FileChannel.open(
        Path.of("input.txt"),
        StandardOpenOption.READ)) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (channel.read(buffer) > 0) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                System.out.print((char) buffer.get());
            }
            buffer.clear();
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

}


