#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>

namespace Ui {
class MainWindow;
}

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    explicit MainWindow(QWidget *parent = 0);
    ~MainWindow();

private slots:
    void on_HC595PIN0_0_clicked();

    void on_HC595PIN0_1_clicked();

    void on_HC595PIN1_0_clicked();

    void on_HC595PIN1_1_clicked();

    void on_HC595PIN2_0_clicked();

    void on_HC595PIN2_1_clicked();

    void on_HC595PIN3_1_clicked();

    void on_HC595PIN3_0_clicked();

    void on_HC595PIN4_0_clicked();

    void on_HC595PIN4_1_clicked();

    void on_HC595PIN5_0_clicked();
    void on_HC595PIN5_1_clicked();
    void on_HC595PIN6_0_clicked();
    void on_HC595PIN6_1_clicked();
    void on_HC595PIN7_0_clicked();
    void on_HC595PIN7_1_clicked();
    void on_HC595PIN8_0_clicked();
    void on_HC595PIN8_1_clicked();
    void on_HC595PIN9_0_clicked();
    void on_HC595PIN9_1_clicked();
    void on_HC595PIN10_0_clicked();
    void on_HC595PIN10_1_clicked();

    void on_HC595PIN11_0_clicked();
    void on_HC595PIN11_1_clicked();
    void on_HC595PIN12_0_clicked();
    void on_HC595PIN12_1_clicked();
    void on_HC595PIN13_0_clicked();
    void on_HC595PIN13_1_clicked();
    void on_HC595PIN14_0_clicked();
    void on_HC595PIN14_1_clicked();
    void on_HC595PIN15_0_clicked();
    void on_HC595PIN15_1_clicked();


    void on_Reset595_clicked();

    void on_Get595_clicked();

private:
    Ui::MainWindow *ui;
};

#endif // MAINWINDOW_H
