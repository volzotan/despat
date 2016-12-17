To install OpenCV (with Java support) through Homebrew, you need to add the science tap to Homebrew: 
brew tap homebrew/science 
and effectively install OpenCV: 
brew install opencv3 --with-contrib --with-java

After the installation of OpenCV, the needed jar file and the dylib library will be located at 
/usr/local/Cellar/opencv3/3.1.0_4/share/OpenCV/java
the .so lib may need to be renamed to .dylib on OSX.

Additionally, the Native Library Location for the .jar has to be set in intellij idea (google).