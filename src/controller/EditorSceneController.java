package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import model.FileManager;
import model.FileManagerInterface;

import java.io.File;
import java.io.*;
import java.util.Optional;

/**
 * Created by SH on 2016-05-11.
 */
public class EditorSceneController {

    @FXML private HighlightEditorInterface editor;

    @FXML private Button btnFileSave;
    @FXML private Button btnFileOpen;
    @FXML private Label filePath;

    private Button btnCompare, btnMergeLeft, btnMergeRight;



    @FXML
    private void initialize(){

        Platform.runLater(()-> {
            _getBtnsReference();
        });
    }

    private void _getBtnsReference(){

        Node root = editor.getScene().getRoot();

        this.btnMergeLeft = (Button)root.lookup("#btnMergeLeft");
        this.btnMergeRight = (Button)root.lookup("#btnMergeRight");
        this.btnCompare = (Button)root.lookup("#btnCompare");

    }

    @FXML // Editor에서 키보드입력이 있을 때의 동작
    private void onTextAreaKeyPressed(KeyEvent event){
        if( event.getCode().compareTo(KeyCode.ENTER) == 0 ){
            System.out.println("enter");
        }
    }

    @FXML // 불러오기 버튼을 클릭했을 때의 동작
    private void onTBBtnLoadClicked(ActionEvent event) {
        //File chooser code

        //TODO 파일이 이미 load되어있고, isEdited==true이면, re-load전에 '저장할래?'물어본다.
        try {
            Stage s = (Stage) btnFileOpen.getScene().getWindow();

            FileExplorer fileLoadExplorer = new FileLoadExplorer();
            File selectedFile = fileLoadExplorer.getDialog(btnFileOpen);

            //선택된 파일의 Text를 해당되는 Edit Pane에 띄워준다.
            FileManagerInterface.SideOfEditor side;

            if( editor.getParent().getParent().getId().contentEquals("leftEditor") ) {
                side = FileManagerInterface.SideOfEditor.Left;
            } else {
                side = FileManagerInterface.SideOfEditor.Right;
            }

            FileManager.getFileManagerInterface().loadFile(selectedFile.getPath(), side);
            editor.setText(side, FileManager.getFileManagerInterface().getString(side));

            filePath.setText(selectedFile.getPath());

        }
        catch(UnsupportedEncodingException e) {                                                                        // TODO Exception 별 처리 필요.
            e.printStackTrace();
            System.out.println("Exception.");
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    @FXML // 저장 버튼을 클릭했을 때의 동작
    private void onTBBtnSaveClicked(ActionEvent event) throws IOException { //UnsupportedEncoingException 추가

        //TODO isEdited가 true인 경우, 버튼 활성화
        try {
                                                                                                    /* 저장 버튼이 속한 패널을 판단하고,
                                                                                                       해당 패널의 Text를 읽어서
                                                                                                       파일 객체를 갱신하고, 파일에 덮어씌운다. */
            if (editor.getParent().getParent().getId().equals("leftEditor")) {
                //TODO isEdited 상태로 저장 여부 결정
                String s = editor.getText();
                FileManager.getFileManagerInterface().saveFile(s, FileManagerInterface.SideOfEditor.Left);

            } else {
                String s = editor.getText();
                //TODO isEdited 상태로 저장 여부 결정
                FileManager.getFileManagerInterface().saveFile(s, FileManagerInterface.SideOfEditor.Right);
            }
            //TODO 파일을 실제로 저장해보면 개행 처리가 안 됨 ㅜㅠ
            FileManager.getFileManagerInterface().cancelCompare();

        } catch (Exception e) { // FileNotFound 등 Exception에 대한 처리.
            //로드가 되지 않은 채로 저장을 눌렀다든가.
            //edit만 하고 저장을 눌렀다든가?

            // TODO 경고창 매서드로 따로 만들기

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Simple Merge - 소공 2팀");
            alert.setHeaderText(null);
            alert.setContentText("새로운 파일로 저장하시겠습니까?");

            ButtonType buttonTypeSave = new ButtonType("저장");
            ButtonType buttonTypeNotSave = new ButtonType("저장 안 함");
            ButtonType buttonTypeCancel = new ButtonType("취소");

            alert.getButtonTypes().setAll(buttonTypeSave, buttonTypeNotSave, buttonTypeCancel);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == buttonTypeSave){

                FileExplorer fileSaveExplorer = new FileSaveExplorer();
                File file = fileSaveExplorer.getDialog(btnFileSave);

                if(editor.getParent().getParent().getParent().getId().equals("leftEditor"))
                    FileManager.getFileManagerInterface().saveFile(editor.getText(), file.getAbsolutePath(), FileManagerInterface.SideOfEditor.Left);
                else
                    FileManager.getFileManagerInterface().saveFile(editor.getText(), file.getAbsolutePath(), FileManagerInterface.SideOfEditor.Right);



            } else if (result.get() == buttonTypeNotSave) {
                // TODO 만들지 않겠다고 하면 EDIT PANE을 비우고, 파일과의 연결을 끊는다.
            } else {
                //취소->아무것도 하지 않음.
            }

            // TODO 성공하면, isEdited를 false로 바꾼다.
            // TODO 실패하면, 실패했다는 알림을 표시하고 진행 중지


        }
    }

    @FXML // 수정 버튼을 클릭했을 때의 동작
    //TODO 나머지 버튼들 활성화/비활성화 조절
    private void onTBBtnEditClicked(ActionEvent event) {
        if(!editor.isEditable()) {    // edit 모드로 진입
            editor.      setEditable(true);
            editor.       changeMode(true);
            btnFileOpen.  setDisable(true);
            btnFileSave.  setDisable(true);
            btnCompare.   setDisable(true);
            btnMergeLeft. setDisable(true);
            btnMergeRight.setDisable(true);
            //TODO STATUS 갱신 editing으로

            if(editor.getParent().getParent().getParent().getId().equals("leftEditor"))
                FileManager.getFileManagerInterface().setEdited(FileManagerInterface.SideOfEditor.Left, true);

            else
                FileManager.getFileManagerInterface().setEdited(FileManagerInterface.SideOfEditor.Right, true);
        }
        else {                          // edit 모드 탈출
            editor.      setEditable(false);
            editor.       changeMode(false);
            btnFileOpen.  setDisable(false);
            btnFileSave.  setDisable(false);
            btnCompare.   setDisable(false);
            btnMergeLeft. setDisable(false);
            btnMergeRight.setDisable(false);

            if(editor.getParent().getParent().getParent().getId().equals("leftEditor")) {
                editor.update(FileManagerInterface.SideOfEditor.Left);
            }
            else {
                editor.update(FileManagerInterface.SideOfEditor.Right);
            }
        }

    }

    //Method for testing
    public void useSaveActionMethod() throws IOException {
        onTBBtnSaveClicked(new ActionEvent());
    }
}
