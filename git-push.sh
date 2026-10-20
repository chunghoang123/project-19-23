#!/usr/bin/env bash
# =============================================================================
#  Đẩy toàn bộ dự án lên GitHub.
#
#  Cách dùng:
#    ./git-push.sh
#
#  Git sẽ hỏi:
#    Username : chunghoang123
#    Password : Personal Access Token (PAT)  — KHÔNG phải mật khẩu GitHub
#
#  Tạo PAT tại:  https://github.com/settings/personal-access-tokens/new
#    Repository access : chỉ chọn project-19-23
#    Permissions       : Contents → Read and write
#
#  Token được cache trong RAM 1 giờ (credential.helper = cache), không ghi ra đĩa.
# =============================================================================
set -euo pipefail

cd "$(dirname "$0")"

echo "╔════════════════════════════════════════════════════════════╗"
echo "║  Đẩy dự án CineX lên GitHub                                 ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo
echo "Nhánh hiện tại : $(git branch --show-current)"
echo "Commit          : $(git log --oneline -1)"
echo "Số file         : $(git ls-files | wc -l)"
echo "Remote          : $(git remote get-url origin)"
echo
echo "→ Nhập thông tin GitHub khi được hỏi:"
echo "    Username : chunghoang123"
echo "    Password : Personal Access Token (PAT)"
echo

git push -u origin main

echo
echo "✔ Đã đẩy lên GitHub thành công:"
echo "  https://github.com/chunghoang123/project-19-23"
