
namespace Wit.Example_JY901
{
    partial class Form1
    {
        /// <summary>
        /// 必需的设计器变量。
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// 清理所有正在使用的资源。
        /// </summary>
        /// <param name="disposing">如果应释放托管资源，为 true；否则为 false。</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows 窗体设计器生成的代码

        /// <summary>
        /// 设计器支持所需的方法 - 不要修改
        /// 使用代码编辑器修改此方法的内容。
        /// </summary>
        private void InitializeComponent()
        {
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(Form1));
            this.portComboBox = new System.Windows.Forms.ComboBox();
            this.baudComboBox = new System.Windows.Forms.ComboBox();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.openButton = new System.Windows.Forms.Button();
            this.closeButton = new System.Windows.Forms.Button();
            this.groupBox2 = new System.Windows.Forms.GroupBox();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.dataRichTextBox = new System.Windows.Forms.RichTextBox();
            this.leftPanel = new System.Windows.Forms.Panel();
            this.groupBox3 = new System.Windows.Forms.GroupBox();
            this.bandWidth256 = new System.Windows.Forms.Button();
            this.bandWidth20 = new System.Windows.Forms.Button();
            this.returnRate50 = new System.Windows.Forms.Button();
            this.returnRate10 = new System.Windows.Forms.Button();
            this.readReg03Button = new System.Windows.Forms.Button();
            this.endFieldCalibrationButton = new System.Windows.Forms.Button();
            this.startFieldCalibrationButton = new System.Windows.Forms.Button();
            this.appliedCalibrationButton = new System.Windows.Forms.Button();
            this.groupBox2.SuspendLayout();
            this.groupBox1.SuspendLayout();
            this.leftPanel.SuspendLayout();
            this.groupBox3.SuspendLayout();
            this.SuspendLayout();
            // 
            // portComboBox
            // 
            resources.ApplyResources(this.portComboBox, "portComboBox");
            this.portComboBox.FormattingEnabled = true;
            this.portComboBox.Name = "portComboBox";
            this.portComboBox.MouseDown += new System.Windows.Forms.MouseEventHandler(this.portComboBox_MouseDown);
            // 
            // baudComboBox
            // 
            resources.ApplyResources(this.baudComboBox, "baudComboBox");
            this.baudComboBox.FormattingEnabled = true;
            this.baudComboBox.Name = "baudComboBox";
            // 
            // label1
            // 
            resources.ApplyResources(this.label1, "label1");
            this.label1.Name = "label1";
            // 
            // label2
            // 
            resources.ApplyResources(this.label2, "label2");
            this.label2.Name = "label2";
            // 
            // openButton
            // 
            resources.ApplyResources(this.openButton, "openButton");
            this.openButton.Name = "openButton";
            this.openButton.UseVisualStyleBackColor = true;
            this.openButton.Click += new System.EventHandler(this.openButton_Click);
            // 
            // closeButton
            // 
            resources.ApplyResources(this.closeButton, "closeButton");
            this.closeButton.Name = "closeButton";
            this.closeButton.UseVisualStyleBackColor = true;
            this.closeButton.Click += new System.EventHandler(this.closeButton_Click);
            // 
            // groupBox2
            // 
            resources.ApplyResources(this.groupBox2, "groupBox2");
            this.groupBox2.Controls.Add(this.label1);
            this.groupBox2.Controls.Add(this.closeButton);
            this.groupBox2.Controls.Add(this.portComboBox);
            this.groupBox2.Controls.Add(this.openButton);
            this.groupBox2.Controls.Add(this.baudComboBox);
            this.groupBox2.Controls.Add(this.label2);
            this.groupBox2.Name = "groupBox2";
            this.groupBox2.TabStop = false;
            // 
            // groupBox1
            // 
            resources.ApplyResources(this.groupBox1, "groupBox1");
            this.groupBox1.Controls.Add(this.dataRichTextBox);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.TabStop = false;
            // 
            // dataRichTextBox
            // 
            resources.ApplyResources(this.dataRichTextBox, "dataRichTextBox");
            this.dataRichTextBox.Name = "dataRichTextBox";
            // 
            // leftPanel
            // 
            resources.ApplyResources(this.leftPanel, "leftPanel");
            this.leftPanel.Controls.Add(this.groupBox3);
            this.leftPanel.Controls.Add(this.groupBox2);
            this.leftPanel.Name = "leftPanel";
            // 
            // groupBox3
            // 
            resources.ApplyResources(this.groupBox3, "groupBox3");
            this.groupBox3.Controls.Add(this.bandWidth256);
            this.groupBox3.Controls.Add(this.bandWidth20);
            this.groupBox3.Controls.Add(this.returnRate50);
            this.groupBox3.Controls.Add(this.returnRate10);
            this.groupBox3.Controls.Add(this.readReg03Button);
            this.groupBox3.Controls.Add(this.endFieldCalibrationButton);
            this.groupBox3.Controls.Add(this.startFieldCalibrationButton);
            this.groupBox3.Controls.Add(this.appliedCalibrationButton);
            this.groupBox3.Name = "groupBox3";
            this.groupBox3.TabStop = false;
            // 
            // bandWidth256
            // 
            resources.ApplyResources(this.bandWidth256, "bandWidth256");
            this.bandWidth256.Name = "bandWidth256";
            this.bandWidth256.UseVisualStyleBackColor = true;
            this.bandWidth256.Click += new System.EventHandler(this.bandWidth256_Click);
            // 
            // bandWidth20
            // 
            resources.ApplyResources(this.bandWidth20, "bandWidth20");
            this.bandWidth20.Name = "bandWidth20";
            this.bandWidth20.UseVisualStyleBackColor = true;
            this.bandWidth20.Click += new System.EventHandler(this.bandWidth20_Click);
            // 
            // returnRate50
            // 
            resources.ApplyResources(this.returnRate50, "returnRate50");
            this.returnRate50.Name = "returnRate50";
            this.returnRate50.UseVisualStyleBackColor = true;
            this.returnRate50.Click += new System.EventHandler(this.returnRate50_Click);
            // 
            // returnRate10
            // 
            resources.ApplyResources(this.returnRate10, "returnRate10");
            this.returnRate10.Name = "returnRate10";
            this.returnRate10.UseVisualStyleBackColor = true;
            this.returnRate10.Click += new System.EventHandler(this.returnRate10_Click);
            // 
            // readReg03Button
            // 
            resources.ApplyResources(this.readReg03Button, "readReg03Button");
            this.readReg03Button.Name = "readReg03Button";
            this.readReg03Button.UseVisualStyleBackColor = true;
            this.readReg03Button.Click += new System.EventHandler(this.readReg03Button_Click);
            // 
            // endFieldCalibrationButton
            // 
            resources.ApplyResources(this.endFieldCalibrationButton, "endFieldCalibrationButton");
            this.endFieldCalibrationButton.Name = "endFieldCalibrationButton";
            this.endFieldCalibrationButton.UseVisualStyleBackColor = true;
            this.endFieldCalibrationButton.Click += new System.EventHandler(this.endFieldCalibrationButton_Click);
            // 
            // startFieldCalibrationButton
            // 
            resources.ApplyResources(this.startFieldCalibrationButton, "startFieldCalibrationButton");
            this.startFieldCalibrationButton.Name = "startFieldCalibrationButton";
            this.startFieldCalibrationButton.UseVisualStyleBackColor = true;
            this.startFieldCalibrationButton.Click += new System.EventHandler(this.startFieldCalibrationButton_Click);
            // 
            // appliedCalibrationButton
            // 
            resources.ApplyResources(this.appliedCalibrationButton, "appliedCalibrationButton");
            this.appliedCalibrationButton.Name = "appliedCalibrationButton";
            this.appliedCalibrationButton.UseVisualStyleBackColor = true;
            this.appliedCalibrationButton.Click += new System.EventHandler(this.appliedCalibrationButton_Click);
            // 
            // Form1
            // 
            resources.ApplyResources(this, "$this");
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Controls.Add(this.groupBox1);
            this.Controls.Add(this.leftPanel);
            this.Name = "Form1";
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.Form1_FormClosing);
            this.Load += new System.EventHandler(this.Form1_Load);
            this.groupBox2.ResumeLayout(false);
            this.groupBox2.PerformLayout();
            this.groupBox1.ResumeLayout(false);
            this.leftPanel.ResumeLayout(false);
            this.groupBox3.ResumeLayout(false);
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.ComboBox portComboBox;
        private System.Windows.Forms.ComboBox baudComboBox;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Button openButton;
        private System.Windows.Forms.Button closeButton;
        private System.Windows.Forms.GroupBox groupBox2;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.RichTextBox dataRichTextBox;
        private System.Windows.Forms.Panel leftPanel;
        private System.Windows.Forms.GroupBox groupBox3;
        private System.Windows.Forms.Button readReg03Button;
        private System.Windows.Forms.Button endFieldCalibrationButton;
        private System.Windows.Forms.Button startFieldCalibrationButton;
        private System.Windows.Forms.Button appliedCalibrationButton;
        private System.Windows.Forms.Button bandWidth256;
        private System.Windows.Forms.Button bandWidth20;
        private System.Windows.Forms.Button returnRate50;
        private System.Windows.Forms.Button returnRate10;
    }
}

