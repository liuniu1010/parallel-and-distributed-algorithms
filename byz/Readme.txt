# This is a homework of the algorihtm course to implement eig tree to achieve byzantine agreement
# Code was written in c# and it needs .net to run.
# Since it is a homework, all classes are grouped into one file according to the requirement.  
# Compile the program with command ( make sure command csc is in PATH )
build.bat
# or use
csc Byz.cs

# Execute the program with command, 
Byz.exe Generals.txt

# Redirect the output to a file
Byz.exe Generals.txt > runlog.txt

# in the folder, two sample files GeneralsN4.txt and GeneralsN7.txt were also provided as test samples
# if we want to test the cases with N=7, use the same format of GeneralsN7.txt, the content can be modified
# the program can support for any number of N, but the input format should be right, or else it will throw exception for error input format.
