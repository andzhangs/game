switch (v.getId()) {
    case R.id.btn1:
        new ActionSheetDialog(context).builder().setTitle("清空消息列表后，聊天记录依然保留，确定要清空消息列表？").setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .addSheetItem("清空消息列表", ActionSheetDialog.SheetItemColor.Red, new ActionSheetDialog.OnSheetItemClickListener() {
                    @Override
                    public void onClick(int which) {
                    }
                }).show();
        break;
    case R.id.btn2:
        new ActionSheetDialog(context).builder().setCancelable(false).setCanceledOnTouchOutside(false)
                .addSheetItem("发送给好友", ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
                    @Override
                    public void onClick(int which) {
                    }
                }).addSheetItem("转载到空间相册", ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
            @Override
            public void onClick(int which) {
            }
        }).addSheetItem("上传到群相册", ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
            @Override
            public void onClick(int which) {
            }
        }).addSheetItem("保存到手机", ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
            @Override
            public void onClick(int which) {
            }
        }).addSheetItem("收藏", ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
            @Override
            public void onClick(int which) {
            }
        }).addSheetItem("查看聊天图片", ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
            @Override
            public void onClick(int which) {
            }
        }).show();
        break;
    case R.id.btn3:
        new ActionSheetDialog(context).builder().setTitle("请选择操作").setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .addSheetItem("条目一", ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
                    @Override
                    public void onClick(int which) {
                        Toast.makeText(context, "item" + which, Toast.LENGTH_SHORT).show();
                    }
                }).addSheetItem("条目二", ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
            @Override
            public void onClick(int which) {
                Toast.makeText(context, "item" + which, Toast.LENGTH_SHORT).show();
            }
        }).addSheetItem("条目三", ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
            @Override
            public void onClick(int which) {
                Toast.makeText(context, "item" + which, Toast.LENGTH_SHORT).show();
            }
        }).addSheetItem("条目四", ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
            @Override
            public void onClick(int which) {
                Toast.makeText(context, "item" + which, Toast.LENGTH_SHORT).show();
            }
        }).addSheetItem("条目五", ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
            @Override
            public void onClick(int which) {
                Toast.makeText(context, "item" + which, Toast.LENGTH_SHORT).show();
            }
        }).addSheetItem("条目六", ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
            @Override
            public void onClick(int which) {
                Toast.makeText(context, "item" + which, Toast.LENGTH_SHORT).show();
            }
        }).addSheetItem("条目七", ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
            @Override
            public void onClick(int which) {
                Toast.makeText(context, "item" + which, Toast.LENGTH_SHORT).show();
            }
        }).addSheetItem("条目八", ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
            @Override
            public void onClick(int which) {
                Toast.makeText(context, "item" + which, Toast.LENGTH_SHORT).show();
            }
        }).addSheetItem("条目九", ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
            @Override
            public void onClick(int which) {
                Toast.makeText(context, "item" + which, Toast.LENGTH_SHORT).show();
            }
        }).addSheetItem("条目十", ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
            @Override
            public void onClick(int which) {
                Toast.makeText(context, "item" + which, Toast.LENGTH_SHORT).show();
            }
        }).show();
        break;
    case R.id.btn4:
        new AlertDialog(context).builder().setTitle("退出当前账号").setMsg("再连续登陆15天，就可变身为QQ达人。退出QQ可能会使你现有记录归零，确定退出？")
                .setCancelable(false).setCanceledOnTouchOutside(false).setPositiveButton("确认退出", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).setNegativeButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).show();
        break;
    case R.id.btn5:
        new AlertDialog(context).builder().setMsg("你现在无法接收到新消息提醒。请到系统-设置-通知中开启消息提醒")
                .setCancelable(false).setCanceledOnTouchOutside(false).setNegativeButton("确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).show();
        break;
    default:
        break;

}
