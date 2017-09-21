package cop5556sp17;

import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

  /**
   * @param DEVEL used as parameter to genPrint and genPrintTOS
   * @param GRADE used as parameter to genPrint and genPrintTOS
   * @param sourceFileName name of source file, may be null.
   */
  public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
    super();
    this.DEVEL = DEVEL;
    this.GRADE = GRADE;
    this.sourceFileName = sourceFileName;
    fieldCount = 0;
    localVarCount = 1;
    listOfDeclarations = new ArrayList<>();
  }

  ClassWriter cw;
  String className;
  String classDesc;
  String sourceFileName;
  ArrayList<Dec> listOfDeclarations;

  MethodVisitor mv; // visitor of method currently under construction

  /** Indicates whether genPrint and genPrintTOS should generate code. */
  final boolean DEVEL;
  final boolean GRADE;
  private int fieldCount;
  private int localVarCount;

  @Override
  public Object visitProgram(Program program, Object arg) throws Exception {
    cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    className = program.getName();
    classDesc = "L" + className + ";";
    String sourceFileName = (String) arg;
    cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
        new String[] {"java/lang/Runnable"});
    cw.visitSource(sourceFileName, null);

    // generate constructor code
    // get a MethodVisitor
    mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null, null);
    mv.visitCode();
    // Create label at start of code
    Label constructorStart = new Label();
    mv.visitLabel(constructorStart);
    // this is for convenience during development--you can see that the code
    // is doing something.
    CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
    // generate code to call superclass constructor
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    // visit parameter decs to add each as field to the class
    // pass in mv so decs can add their initialization code to the
    // constructor.
    ArrayList<ParamDec> paramDeclarations = program.getParams();
    for (ParamDec paramDeclaration : paramDeclarations)
      paramDeclaration.visit(this, mv);
    mv.visitInsn(RETURN);
    // create label at end of code
    Label constructorEnd = new Label();
    mv.visitLabel(constructorEnd);
    // finish up by visiting local vars of constructor
    // the fourth and fifth arguments are the region of code where the local
    // variable is defined as represented by the labels we inserted.
    mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
    mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
    // indicates the max stack size for the method.
    // because we used the COMPUTE_FRAMES parameter in the classwriter
    // constructor, asm
    // will do this for us. The parameters to visitMaxs don't matter, but
    // the method must
    // be called.
    mv.visitMaxs(1, 1);
    // finish up code generation for this method.
    mv.visitEnd();
    // end of constructor

    // create main method which does the following
    // 1. instantiate an instance of the class being generated, passing the
    // String[] with command line arguments
    // 2. invoke the run method.
    mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
    mv.visitCode();
    Label mainStart = new Label();
    mv.visitLabel(mainStart);
    // this is for convenience during development--you can see that the code
    // is doing something.
    CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
    mv.visitTypeInsn(NEW, className);
    mv.visitInsn(DUP);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
    mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
    mv.visitInsn(RETURN);
    Label mainEnd = new Label();
    mv.visitLabel(mainEnd);
    mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
    mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
    mv.visitMaxs(0, 0);
    mv.visitEnd();

    // create run method
    mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
    mv.visitCode();
    Label startRun = new Label();
    mv.visitLabel(startRun);
    CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
    program.getB().visit(this, null);
    mv.visitInsn(RETURN);
    Label endRun = new Label();
    mv.visitLabel(endRun);
    mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
    for (Dec declaration : listOfDeclarations) {
      mv.visitLocalVariable(declaration.getIdent().getText(), classDesc, null,
          declaration.getScopeStart(), declaration.getScopeEnd(), declaration.getSlot());
    }
    mv.visitMaxs(1, 1);
    mv.visitEnd(); // end of run method

    cw.visitEnd();// end of class

    // generate classfile and return it
    return cw.toByteArray();
  }

  @Override
  public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
    Chain leftChainElement = binaryChain.getE0();
    if (leftChainElement instanceof IdentChain) {
      arg = true;
    }
    leftChainElement.visit(this, true);
    if (leftChainElement.getType() == TypeName.URL) {
      mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL",
          PLPRuntimeImageIO.readFromURLSig, false);
    } else if (leftChainElement.getType() == TypeName.FILE) {
      mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile",
          PLPRuntimeImageIO.readFromFileDesc, false);
    }
    binaryChain.getE1().visit(this, false);
    if (binaryChain.getArrow().kind == Kind.BARARROW
        && binaryChain.getE1() instanceof FilterOpChain) {
      if (binaryChain.getE0() instanceof IdentChain) {
        mv.visitInsn(DUP);
        mv.visitVarInsn(ASTORE, ((IdentChain) binaryChain.getE0()).getDec().getSlot());
      }
    }
    return null;
  }

  @Override
  public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
    FieldVisitor fv;
    MethodVisitor mv = (MethodVisitor) arg;
    String paramName = paramDec.getIdent().getText();
    String methodOwner = "";
    String methodName = "";
    String methodDesc = "";
    TypeName type = Type.getTypeName(paramDec.getType());
    fv = cw.visitField(0, paramName, type.getJVMTypeDesc(), null, null);
    fv.visitEnd();
    if (type == TypeName.INTEGER) {
      methodOwner = "java/lang/Integer";
      methodName = "parseInt";
      methodDesc = "(Ljava/lang/String;)I";
      paramDeclarationVisitCommon(mv, paramName, methodOwner, methodName, methodDesc, type);
    } else if (type == TypeName.BOOLEAN) {
      methodOwner = "java/lang/Boolean";
      methodName = "parseBoolean";
      methodDesc = "(Ljava/lang/String;)Z";
      paramDeclarationVisitCommon(mv, paramName, methodOwner, methodName, methodDesc, type);
    } else if (type == TypeName.URL) {
      mv.visitVarInsn(ALOAD, 0);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitLdcInsn(fieldCount);
      mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL",
          PLPRuntimeImageIO.getURLSig, false);
      mv.visitFieldInsn(PUTFIELD, className, paramName, PLPRuntimeImageIO.URLDesc);
      fieldCount++;
    } else if (type == TypeName.FILE) {
      mv.visitVarInsn(ALOAD, 0);
      mv.visitTypeInsn(NEW, "java/io/File");
      mv.visitInsn(DUP);
      mv.visitVarInsn(ALOAD, 1);
      mv.visitLdcInsn(fieldCount);
      mv.visitInsn(AALOAD);
      mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
      mv.visitFieldInsn(PUTFIELD, className, paramName, PLPRuntimeImageIO.FileDesc);
      fieldCount++;
    }
    return null;

  }

  private void paramDeclarationVisitCommon(MethodVisitor mv, String paramName, String methodOwner,
      String methodName, String methodDesc, TypeName type) {
    mv.visitVarInsn(ALOAD, 0);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitLdcInsn(fieldCount);
    mv.visitInsn(AALOAD);
    mv.visitMethodInsn(INVOKESTATIC, methodOwner, methodName, methodDesc, false);
    mv.visitFieldInsn(PUTFIELD, className, paramName, type.getJVMTypeDesc());
    fieldCount++;
  }

  @Override
  public Object visitBlock(Block block, Object arg) throws Exception {
    Label scopeStart = new Label();
    Label scopeEnd = new Label();
    mv.visitLabel(scopeStart);
    for (Dec d : block.getDecs()) {
      d.setScopeStart(scopeStart);
      d.setScopeEnd(scopeEnd);
      d.visit(this, arg);
    }
    for (Statement s : block.getStatements()) {
      s.visit(this, arg);
      if (s instanceof BinaryChain) {
        mv.visitInsn(POP);
      }
    }
    mv.visitLabel(scopeEnd);
    return null;
  }

  @Override
  public Object visitDec(Dec declaration, Object arg) throws Exception {
    declaration.setSlot(localVarCount);
    listOfDeclarations.add(declaration);
    if (Type.getTypeName(declaration.getType()) == TypeName.FRAME
        || Type.getTypeName(declaration.getType()) == TypeName.IMAGE) {
      mv.visitInsn(ACONST_NULL);
      mv.visitVarInsn(ASTORE, localVarCount);
    }
    localVarCount++;
    return null;
  }

  @Override
  public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
    ifStatement.getE().visit(this, arg);
    Label labelAfter = new Label();
    mv.visitJumpInsn(IFEQ, labelAfter);
    Label labelBlock = new Label();
    mv.visitLabel(labelBlock);
    ifStatement.getB().visit(this, arg);
    mv.visitLabel(labelAfter);
    return null;
  }

  @Override
  public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
    Label labelBeforeWhile = new Label();
    mv.visitLabel(labelBeforeWhile);
    whileStatement.getE().visit(this, arg);
    Label labelAfterWhile = new Label();
    mv.visitJumpInsn(IFEQ, labelAfterWhile);
    Label labelBlock = new Label();
    mv.visitLabel(labelBlock);
    whileStatement.getB().visit(this, arg);
    mv.visitJumpInsn(GOTO, labelBeforeWhile);
    mv.visitLabel(labelAfterWhile);
    return null;
  }

  @Override
  public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg)
      throws Exception {
    assignStatement.getE().visit(this, arg);
    CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
    CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getType());
    assignStatement.getVar().visit(this, arg);
    return null;
  }

  @Override
  public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
    if (identX.getDec() instanceof ParamDec) {
      mv.visitVarInsn(ALOAD, 0);
      mv.visitInsn(SWAP);
      mv.visitFieldInsn(PUTFIELD, className, identX.getDec().getIdent().getText(),
          identX.getType().getJVMTypeDesc());
    } else {
      if (identX.getType() == TypeName.IMAGE) {
        mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage",
            PLPRuntimeImageOps.copyImageSig, false);
        mv.visitVarInsn(ASTORE, identX.getDec().getSlot());
      } else if (identX.getType() == TypeName.FRAME) {
        mv.visitVarInsn(ASTORE, identX.getDec().getSlot());
      } else {
        mv.visitVarInsn(ISTORE, identX.getDec().getSlot());
      }
    }
    return null;

  }

  @Override
  public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg)
      throws Exception {
    TypeName t0 = binaryExpression.getE0().getType();
    TypeName t1 = binaryExpression.getE1().getType();
    TypeName t = binaryExpression.getType();
    Kind opKind = binaryExpression.getOp().kind;
    binaryExpression.getE0().visit(this, arg);
    binaryExpression.getE1().visit(this, arg);
    if (t == TypeName.BOOLEAN) {
      if (t0 == TypeName.INTEGER && t1 == TypeName.INTEGER) {
        switch (opKind) {
          case LT:
            callVisitIntegerOpToBooleanInstructions(IF_ICMPGE);
            break;
          case GT:
            callVisitIntegerOpToBooleanInstructions(IF_ICMPLE);
            break;
          case LE:
            callVisitIntegerOpToBooleanInstructions(IF_ICMPGT);
            break;
          case GE:
            callVisitIntegerOpToBooleanInstructions(IF_ICMPLT);
            break;
          case EQUAL:
            callVisitIntegerOpToBooleanInstructions(IF_ICMPNE);
            break;
          case NOTEQUAL:
            callVisitIntegerOpToBooleanInstructions(IF_ICMPEQ);
            break;
          default:
            throw new Exception("The given operation is not valid for the Integer type");
        }
      } else if (t0 == TypeName.BOOLEAN && t1 == TypeName.BOOLEAN) {
        switch (opKind) {
          case LT:
            callVisitBooleanOpToBooleanInstructions(IF_ICMPGE);
            break;
          case GT:
            callVisitBooleanOpToBooleanInstructions(IF_ICMPLE);
            break;
          case LE:
            callVisitBooleanOpToBooleanInstructions(IF_ICMPGT);
            break;
          case GE:
            callVisitBooleanOpToBooleanInstructions(IF_ICMPLT);
            break;
          case EQUAL:
            callVisitBooleanOpToBooleanInstructions(IF_ICMPNE);
            break;
          case NOTEQUAL:
            callVisitBooleanOpToBooleanInstructions(IF_ICMPEQ);
            break;
          case AND:
            mv.visitInsn(IAND);
            break;
          case OR:
            mv.visitInsn(IOR);
            break;
          default:
            throw new Exception("The given operation is not valid for the Integer type");
        }
      } else if (t0 == t1) {
        switch (opKind) {
          case EQUAL:
            Label l4 = new Label();
            mv.visitJumpInsn(IF_ACMPNE, l4);
            mv.visitInsn(ICONST_1);
            Label l5 = new Label();
            mv.visitJumpInsn(GOTO, l5);
            mv.visitLabel(l4);
            mv.visitInsn(ICONST_0);
            mv.visitLabel(l5);
            break;
          case NOTEQUAL:
            Label l7 = new Label();
            mv.visitJumpInsn(IF_ACMPEQ, l7);
            mv.visitInsn(ICONST_1);
            Label l8 = new Label();
            mv.visitJumpInsn(GOTO, l8);
            mv.visitLabel(l7);
            mv.visitInsn(ICONST_0);
            mv.visitLabel(l8);
            break;
          default:
            throw new Exception("The given operation is not valid for comparing two images");
        }
      } else {
        throw new UnsupportedOperationException(
            "The given operation is not valid for the " + t0 + " type " + " and " + t1 + " type");
      }
    } else if (t == TypeName.INTEGER) {
      if (t0 == TypeName.INTEGER && t1 == TypeName.INTEGER) {
        switch (opKind) {
          case PLUS:
            mv.visitInsn(IADD);
            break;
          case MINUS:
            mv.visitInsn(ISUB);
            break;
          case TIMES:
            mv.visitInsn(IMUL);
            break;
          case DIV:
            mv.visitInsn(IDIV);
            break;
          case MOD:
            mv.visitInsn(IREM);
            break;
          default:
            throw new UnsupportedOperationException("The given operation is not valid for the " + t0
                + " type " + " and " + t1 + " type");
        }
      }
    } else if (t == TypeName.IMAGE) {
      if (t0 == TypeName.IMAGE && t1 == TypeName.IMAGE) {
        switch (opKind) {
          case PLUS:
            mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "add",
                PLPRuntimeImageOps.addSig, false);
            break;
          case MINUS:
            mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "sub",
                PLPRuntimeImageOps.subSig, false);
            break;
          default:
            throw new UnsupportedOperationException("The given operation is not valid for the " + t0
                + " type " + " and " + t1 + " type");
        }
      } else {
        if (t0 == TypeName.INTEGER && t1 == TypeName.IMAGE) {
          mv.visitInsn(SWAP);
        }
        switch (opKind) {
          case TIMES:
            mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul",
                PLPRuntimeImageOps.mulSig, false);
            break;
          case DIV:
            mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div",
                PLPRuntimeImageOps.divSig, false);
            break;
          case MOD:
            mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod",
                PLPRuntimeImageOps.modSig, false);
            break;
          default:
            throw new UnsupportedOperationException("The given operation is not valid for the " + t0
                + " type " + " and " + t1 + " type");
        }
      }
    }
    return null;
  }

  private void callVisitBooleanOpToBooleanInstructions(int opcode) {
    Label label1 = new Label();
    mv.visitJumpInsn(opcode, label1);
    mv.visitInsn(ICONST_1);
    Label label2 = new Label();
    mv.visitJumpInsn(GOTO, label2);
    mv.visitLabel(label1);
    mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
    mv.visitInsn(ICONST_0);
    mv.visitLabel(label2);
    mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
  }

  private void callVisitIntegerOpToBooleanInstructions(int opcode) {
    Label label1 = new Label();
    mv.visitJumpInsn(opcode, label1);
    mv.visitInsn(ICONST_1);
    Label label2 = new Label();
    mv.visitJumpInsn(GOTO, label2);
    mv.visitLabel(label1);
    mv.visitFrame(Opcodes.F_APPEND, 2, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER}, 0, null);
    mv.visitInsn(ICONST_0);
    mv.visitLabel(label2);
    mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
  }

  @Override
  public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
    if (identExpression.getDec() instanceof ParamDec) {
      mv.visitVarInsn(ALOAD, 0);
      mv.visitFieldInsn(GETFIELD, className, identExpression.getDec().getIdent().getText(),
          identExpression.getType().getJVMTypeDesc());
    } else {
      if (identExpression.getType() == TypeName.IMAGE
          || identExpression.getType() == TypeName.FRAME) {
        mv.visitVarInsn(ALOAD, identExpression.getDec().getSlot());
      } else {
        mv.visitVarInsn(ILOAD, identExpression.getDec().getSlot());
      }
    }
    return null;
  }

  @Override
  public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg)
      throws Exception {
    if (booleanLitExpression.getValue() == false) {
      mv.visitInsn(ICONST_0);
    } else {
      mv.visitInsn(ICONST_1);
    }
    return null;
  }

  @Override
  public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg)
      throws Exception {
    mv.visitLdcInsn(intLitExpression.value);
    return null;
  }

  @Override
  public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
    if (constantExpression.getFirstToken().getText().equals(Kind.KW_SCREENHEIGHT.getText())) {
      mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "getScreenHeight", "()I",
          false);
    } else if (constantExpression.getFirstToken().getText().equals(Kind.KW_SCREENWIDTH.getText())) {
      mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "getScreenWidth", "()I",
          false);
    }
    return null;
  }

  @Override
  public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
    filterOpChain.getArg().visit(this, arg);
    if (filterOpChain.getFirstToken().getText().equals(Kind.OP_BLUR.getText())) {
      mv.visitInsn(ACONST_NULL);
      mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "blurOp",
          "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;",
          false);
    } else if (filterOpChain.getFirstToken().getText().equals(Kind.OP_CONVOLVE.getText())) {
      mv.visitInsn(ACONST_NULL);
      mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "convolveOp",
          "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;",
          false);

    } else if (filterOpChain.getFirstToken().getText().equals(Kind.OP_GRAY.getText())) {
      mv.visitInsn(ACONST_NULL);
      mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "grayOp",
          "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;",
          false);
    }
    return null;
  }

  @Override
  public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
    frameOpChain.getArg().visit(this, arg);
    if (frameOpChain.getFirstToken().getText().equals(Kind.KW_XLOC.getText())) {
      mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getXVal",
          PLPRuntimeFrame.getXValDesc, false);
    } else if (frameOpChain.getFirstToken().getText().equals(Kind.KW_YLOC.getText())) {
      mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getYVal",
          PLPRuntimeFrame.getYValDesc, false);
    } else if (frameOpChain.getFirstToken().getText().equals(Kind.KW_SHOW.getText())) {
      mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "showImage",
          PLPRuntimeFrame.showImageDesc, false);
    } else if (frameOpChain.getFirstToken().getText().equals(Kind.KW_HIDE.getText())) {
      mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "hideImage",
          PLPRuntimeFrame.hideImageDesc, false);
    } else if (frameOpChain.getFirstToken().getText().equals(Kind.KW_MOVE.getText())) {
      mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "moveFrame",
          PLPRuntimeFrame.moveFrameDesc, false);
    }
    return null;
  }

  @Override
  public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
    boolean isLeftChain = (boolean) arg;
    if (isLeftChain) {
      if (identChain.getDec() instanceof ParamDec) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),
            identChain.getType().getJVMTypeDesc());
      } else {
        if (identChain.getType() == TypeName.IMAGE || identChain.getType() == TypeName.FRAME) {
          mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
        } else {
          mv.visitVarInsn(ILOAD, identChain.getDec().getSlot());
        }
      }
    } else {
      if (identChain.getType() == TypeName.INTEGER) {
        mv.visitInsn(DUP);
        if (identChain.getDec() instanceof ParamDec) {
          mv.visitVarInsn(ALOAD, 0);
          mv.visitInsn(SWAP);
          mv.visitFieldInsn(PUTFIELD, className, identChain.getDec().getIdent().getText(),
              identChain.getType().getJVMTypeDesc());
        } else {
          mv.visitVarInsn(ISTORE, identChain.getDec().getSlot());
        }
      } else if (identChain.getType() == TypeName.IMAGE) {
        mv.visitInsn(DUP);
        mv.visitVarInsn(ASTORE, identChain.getDec().getSlot());
      } else if (identChain.getType() == TypeName.FILE) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),
            "Ljava/io/File;");
        mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "write",
            "(Ljava/awt/image/BufferedImage;Ljava/io/File;)Ljava/awt/image/BufferedImage;", false);
      } else if (identChain.getType() == TypeName.FRAME) {
        mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
        mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame",
            "(Ljava/awt/image/BufferedImage;Lcop5556sp17/PLPRuntimeFrame;)Lcop5556sp17/PLPRuntimeFrame;",
            false);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ASTORE, identChain.getDec().getSlot());
      }
    }
    return null;
  }

  @Override
  public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
    imageOpChain.getArg().visit(this, arg);
    if (imageOpChain.getFirstToken().kind == Kind.KW_SCALE) {
      mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale",
          PLPRuntimeImageOps.scaleSig, false);
    } else if (imageOpChain.getFirstToken().kind == Kind.OP_WIDTH) {
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getHeight", "()I", false);
    } else {
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getWidth", "()I", false);
    }
    return null;
  }

  @Override
  public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
    sleepStatement.getE().visit(this, arg);
    mv.visitInsn(I2L);
    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
    return null;
  }

  @Override
  public Object visitTuple(Tuple tuple, Object arg) throws Exception {
    for (Expression e : tuple.getExprList()) {
      e.visit(this, arg);
    }
    return null;
  }

}
